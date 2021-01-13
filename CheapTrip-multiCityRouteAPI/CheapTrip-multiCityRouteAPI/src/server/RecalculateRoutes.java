package server;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import server.model.DirectRoute;
import server.model.Location;
import server.util.DBUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecalculateRoutes implements Runnable {

    public static final String DB_NAME = "cheap_trip";

    public static final String[] allowedTransportationTypes = {"(2,3,5,7,8,9,10)", "(1,2,3,5,7,8,9,10)", "(1)"};
    public static final String[] saveToTables = {"fixed_routes", "routes", "flying_routes"};


    @Override
    public void run() {
        for(int i = 0; i < allowedTransportationTypes.length; i++) {
            calculateRoutes(allowedTransportationTypes[i], saveToTables[i]);
        }
    }

    private void calculateRoutes(String allowedTransportationTypes, String saveToTable) {
        try {
            System.out.println("Started scanning routes");
            Context initContext = new InitialContext();
            Context envContext  = (Context)initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource)envContext.lookup("jdbc/" + DB_NAME);
            Connection conn = ds.getConnection();

            PreparedStatement statement = conn.prepareStatement("select * from locations");
            statement.execute();
            ResultSet locationsResultSet = statement.getResultSet();

            ArrayList<Location> locations = new ArrayList<>();

            SimpleDirectedWeightedGraph<Integer, DefaultEdge> routeGraph = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);

            while (locationsResultSet.next()) {
                int id = locationsResultSet.getInt("id");
                String name = locationsResultSet.getString("name");
                locations.add(new Location(id, name));
                routeGraph.addVertex(id);
            }

            statement = conn.prepareStatement("select `from`, `to`, `euro_price` from " + DBUtils.TRAVEL_DATA_TABLE + " where transportation_type in " + allowedTransportationTypes);
            statement.execute();
            ResultSet travelDataResultSet = statement.getResultSet();
            while (travelDataResultSet.next()) {
                int fromID = travelDataResultSet.getInt("from");
                int toID = travelDataResultSet.getInt("to");
                float price = travelDataResultSet.getFloat("euro_price");

                DefaultEdge e = routeGraph.getEdge(fromID, toID);
                if(e != null) {
                    System.out.println("Updating Price from: " + fromID + ", to: " + toID);
                    if(routeGraph.getEdgeWeight(e) > price)
                        routeGraph.setEdgeWeight(e, price);
                }
                else {
                    System.out.println("Adding to graph from: " + fromID + ", to: " + toID);
                    e = routeGraph.addEdge(fromID, toID);
                    routeGraph.setEdgeWeight(e, price);
                }
            }

            for(Location from : locations){
                System.out.println("Scanning from: " + from);

                for(Location to : locations){
                    if(to.getId() == from.getId())
                            continue;
                    System.out.println("--Scanning route from: " + from + " to: " + to);
                    GraphPath<Integer, DefaultEdge> path = DijkstraShortestPath.findPathBetween(routeGraph, from.getId(), to.getId());

                    StringBuilder query = new StringBuilder("select * from " + DBUtils.TRAVEL_DATA_TABLE + " where transportation_type in " + allowedTransportationTypes + " and (");
                    if(path == null)
                        continue;

                    List<DefaultEdge> edgeList = path.getEdgeList();
                    if(edgeList == null || edgeList.size() == 0)
                        continue;

                    for(int i = 0; i < edgeList.size(); i++){
                        DefaultEdge edge = edgeList.get(i);
                        int edgeFrom = routeGraph.getEdgeSource(edge);
                        int edgeTo = routeGraph.getEdgeTarget(edge);
                        if(i != 0)
                            query.append("OR ");
                        query.append("(`from` = ").append(edgeFrom).append(" and `to` = ").append(edgeTo).append(") ");
                    }
                    query.append(") ORDER BY FIELD(`from`, ");
                    for(int i = 0; i < edgeList.size(); i++){
                        int edgeFrom = routeGraph.getEdgeSource(edgeList.get(i));
                        if(i != 0)
                            query.append(", ");
                        query.append(edgeFrom);
                    }
                    query.append(")");

                    System.out.println("----get direct routes query: " + query);
                    ArrayList<DirectRoute> directRoutes = new ArrayList<>();
                    statement = conn.prepareStatement(query.toString());
                    statement.execute();
                    ResultSet pathResultSet = statement.getResultSet();
                    float totalPrice = 0;
                    StringBuilder travelData = new StringBuilder();

                    int currentFromID = -1, currentToID = -1, bestTravelOptionID = -1;
                    float minPrice = -1;
                    while (pathResultSet.next()) {
                        int id = pathResultSet.getInt("id");
                        int fromID = pathResultSet.getInt("from");
                        int toID = pathResultSet.getInt("to");
                        float euroPrice = pathResultSet.getFloat("euro_price");

                        if(currentFromID == -1){
                            currentFromID = fromID;
                            currentToID = toID;
                            minPrice = euroPrice;
                            bestTravelOptionID = id;
                        }
                        else if(currentFromID == fromID){
                            if(minPrice > euroPrice){
                                minPrice = euroPrice;
                                bestTravelOptionID = id;
                            }
                        }
                        else {
                            totalPrice += minPrice;
                            if (travelData.length() > 0)
                                travelData.append(",");
                            travelData.append(bestTravelOptionID);
                            DirectRoute directRoute = new DirectRoute(bestTravelOptionID, currentFromID, currentToID, minPrice);
                            directRoutes.add(directRoute);
                            System.out.println("----Travel: " + directRoutes);

                            currentFromID = fromID;
                            currentToID = toID;
                            minPrice = euroPrice;
                            bestTravelOptionID = id;
                        }
                    }

                    totalPrice += minPrice;
                    if (travelData.length() > 0)
                        travelData.append(",");
                    travelData.append(bestTravelOptionID);
                    DirectRoute directRoute = new DirectRoute(bestTravelOptionID, currentFromID, currentToID, minPrice);
                    directRoutes.add(directRoute);
                    System.out.println("----Travel: " + directRoutes);


                    statement = conn.prepareStatement("insert into "+saveToTable+" (`from`, `to`, `euro_price`, `travel_data`) values (" + from.getId() + ", " + to.getId() + ", " + totalPrice + ", '" + travelData +  "')");
                    statement.execute();

                }

            }
            conn.close();

        } catch (NamingException | SQLException e) {
            e.printStackTrace();
        }
    }

}