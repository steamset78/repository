CREATE TABLE `currencies` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `name` varchar(100) NOT NULL,
                              `code` varchar(10) NOT NULL,
                              `symbol` varchar(10) NOT NULL,
                              `one_euro_rate` float DEFAULT NULL,
                              `r2r_symbol` varchar(10) DEFAULT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;





CREATE TABLE `locations` (
                             `id` int NOT NULL AUTO_INCREMENT,
                             `name` varchar(100) NOT NULL,
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;






CREATE TABLE `transportation_types` (
                                        `id` int NOT NULL,
                                        `name` varchar(20) NOT NULL,
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;





CREATE TABLE `travel_data` (
                               `id` int NOT NULL AUTO_INCREMENT,
                               `from` int NOT NULL,
                               `to` int NOT NULL,
                               `transportation_type` int DEFAULT NULL,
                               `line` text,
                               `time_in_minutes` int DEFAULT NULL,
                               `price` int DEFAULT NULL,
                               `currency_id` int NOT NULL,
                               `euro_price` float NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `currency` (`currency_id`),
                               KEY `travel_data_ffk_idx` (`from`),
                               KEY `travel_data_tfk_idx` (`to`),
                               CONSTRAINT `travel_data_ffk` FOREIGN KEY (`from`) REFERENCES `locations` (`id`),
                               CONSTRAINT `travel_data_ibfk_1` FOREIGN KEY (`currency_id`) REFERENCES `currencies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                               CONSTRAINT `travel_data_tfk` FOREIGN KEY (`to`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;






CREATE TABLE `fixed_routes` (
                                `id` int NOT NULL AUTO_INCREMENT,
                                `from` int NOT NULL,
                                `to` int NOT NULL,
                                `euro_price` float NOT NULL,
                                `travel_data` varchar(1000) NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `route_from_to_index` (`from`,`to`),
                                KEY `travel_data_frffk_idx` (`from`),
                                KEY `travel_data_frtfk_idx` (`to`),
                                KEY `travel_data_flrffk_idx` (`from`),
                                KEY `travel_data_flrtfk_idx` (`to`),
                                CONSTRAINT `travel_data_frffk` FOREIGN KEY (`from`) REFERENCES `locations` (`id`),
                                CONSTRAINT `travel_data_frtfk` FOREIGN KEY (`to`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;









CREATE TABLE `flying_routes` (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `from` int NOT NULL,
                                 `to` int NOT NULL,
                                 `euro_price` float NOT NULL,
                                 `travel_data` varchar(1000) NOT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `route_from_to_index` (`from`,`to`),
                                 KEY `travel_data_flrtfk` (`to`),
                                 CONSTRAINT `travel_data_flrffk` FOREIGN KEY (`from`) REFERENCES `locations` (`id`),
                                 CONSTRAINT `travel_data_flrtfk` FOREIGN KEY (`to`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;









CREATE TABLE `routes` (
                          `id` int NOT NULL AUTO_INCREMENT,
                          `from` int NOT NULL,
                          `to` int NOT NULL,
                          `euro_price` float NOT NULL,
                          `travel_data` varchar(1000) NOT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `route_from_to_index` (`from`,`to`),
                          KEY `travel_data_ffk_idx` (`from`),
                          KEY `travel_data_tfk_idx` (`to`),
                          KEY `travel_data_rffk_idx` (`from`),
                          KEY `travel_data_rtfk_idx` (`to`),
                          CONSTRAINT `travel_data_rffk` FOREIGN KEY (`from`) REFERENCES `locations` (`id`),
                          CONSTRAINT `travel_data_rtfk` FOREIGN KEY (`to`) REFERENCES `locations` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;