SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `attribute`;
CREATE TABLE `attribute` (
  `attributetype` varchar(16) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(128) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `localname` varchar(128) NOT NULL,
  `namespace` varchar(128) NOT NULL,
  `booleanvalue` tinyint(4) DEFAULT NULL,
  `textvalue` longtext,
  `intvalue` bigint(20) DEFAULT NULL,
  `stringvalue` varchar(2048) DEFAULT NULL,
  `binvalue` longblob,
  `decvalue` decimal(19,6) DEFAULT NULL,
  `datevalue` datetime DEFAULT NULL,
  `tzvalue` varchar(32) DEFAULT NULL,
  `itemid` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `itemid` (`itemid`,`namespace`,`localname`),
  KEY `idx_attrns` (`namespace`),
  KEY `idx_attrname` (`localname`),
  KEY `idx_attrtype` (`attributetype`),
  CONSTRAINT `FKC7AA9CFF55C69C` FOREIGN KEY (`itemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `collection_item`;
CREATE TABLE `collection_item` (
  `createdate` bigint(20) NOT NULL,
  `itemid` bigint(20) NOT NULL,
  `collectionid` bigint(20) NOT NULL,
  PRIMARY KEY (`collectionid`,`itemid`),
  KEY `FK3F30F814FF55C69C` (`itemid`),
  CONSTRAINT `FK3F30F8144D31C165` FOREIGN KEY (`collectionid`) REFERENCES `item` (`id`),
  CONSTRAINT `FK3F30F814FF55C69C` FOREIGN KEY (`itemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `content_data`;
CREATE TABLE `content_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `dictionary_values`;
CREATE TABLE `dictionary_values` (
  `attributeid` bigint(20) NOT NULL,
  `stringvalue` varchar(2048) DEFAULT NULL,
  `keyname` varchar(255) NOT NULL,
  PRIMARY KEY (`attributeid`,`keyname`),
  CONSTRAINT `FK63A274EB579FCDE2` FOREIGN KEY (`attributeid`) REFERENCES `attribute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `event_log`;
CREATE TABLE `event_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authid` bigint(20) NOT NULL,
  `authtype` varchar(64) NOT NULL,
  `entrydate` bigint(20) DEFAULT NULL,
  `id1` bigint(20) DEFAULT NULL,
  `id2` bigint(20) DEFAULT NULL,
  `id3` bigint(20) DEFAULT NULL,
  `id4` bigint(20) DEFAULT NULL,
  `strval1` varchar(255) DEFAULT NULL,
  `strval2` varchar(255) DEFAULT NULL,
  `strval3` varchar(255) DEFAULT NULL,
  `strval4` varchar(255) DEFAULT NULL,
  `eventtype` varchar(64) NOT NULL,
  `uid1` varchar(255) DEFAULT NULL,
  `uid2` varchar(255) DEFAULT NULL,
  `uid3` varchar(255) DEFAULT NULL,
  `uid4` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `event_stamp`;
CREATE TABLE `event_stamp` (
  `icaldata` longtext NOT NULL,
  `enddate` varchar(16) DEFAULT NULL,
  `isfloating` tinyint(4) DEFAULT NULL,
  `isrecurring` tinyint(4) DEFAULT NULL,
  `startdate` varchar(16) DEFAULT NULL,
  `stampid` bigint(20) NOT NULL,
  PRIMARY KEY (`stampid`),
  KEY `idx_floating` (`isfloating`),
  KEY `idx_recurring` (`isrecurring`),
  KEY `idx_startdt` (`startdate`),
  KEY `idx_enddt` (`enddate`),
  CONSTRAINT `FK1ACFBDDE227B4573` FOREIGN KEY (`stampid`) REFERENCES `stamp` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `itemtype` varchar(16) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `clientcreatedate` bigint(20) DEFAULT NULL,
  `clientmodifieddate` bigint(20) DEFAULT NULL,
  `displayname` varchar(1024) DEFAULT NULL,
  `itemname` varchar(255) NOT NULL,
  `uid` varchar(128) NOT NULL,
  `version` int(11) NOT NULL,
  `lastmodification` int(11) DEFAULT NULL,
  `lastmodifiedby` varchar(255) DEFAULT NULL,
  `needsreply` tinyint(4) DEFAULT NULL,
  `sent` tinyint(4) DEFAULT NULL,
  `isautotriage` tinyint(4) DEFAULT NULL,
  `triagestatuscode` int(11) DEFAULT NULL,
  `triagestatusrank` decimal(12,2) DEFAULT NULL,
  `icaluid` varchar(255) DEFAULT NULL,
  `contentEncoding` varchar(32) DEFAULT NULL,
  `contentLanguage` varchar(32) DEFAULT NULL,
  `contentLength` bigint(20) DEFAULT NULL,
  `contentType` varchar(64) DEFAULT NULL,
  `hasmodifications` tinyint(4) DEFAULT NULL,
  `ownerid` bigint(20) NOT NULL,
  `contentdataid` bigint(20) DEFAULT NULL,
  `modifiesitemid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`),
  KEY `idx_itemname` (`itemname`),
  KEY `idx_itemtype` (`itemtype`),
  KEY `FK317B137B89A346` (`contentdataid`),
  KEY `FK317B13FFE49D06` (`modifiesitemid`),
  KEY `FK317B136BE46F4` (`ownerid`),
  CONSTRAINT `FK317B136BE46F4` FOREIGN KEY (`ownerid`) REFERENCES `users` (`id`),
  CONSTRAINT `FK317B137B89A346` FOREIGN KEY (`contentdataid`) REFERENCES `content_data` (`id`),
  CONSTRAINT `FK317B13FFE49D06` FOREIGN KEY (`modifiesitemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DROP TABLE IF EXISTS `multistring_values`;
CREATE TABLE `multistring_values` (
  `attributeid` bigint(20) NOT NULL,
  `stringvalue` varchar(2048) DEFAULT NULL,
  KEY `FK27556477EF2E8A2F` (`attributeid`),
  CONSTRAINT `FK27556477EF2E8A2F` FOREIGN KEY (`attributeid`) REFERENCES `attribute` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `pwrecovery`;
CREATE TABLE `pwrecovery` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creationdate` datetime DEFAULT NULL,
  `pwrecoverykey` varchar(255) NOT NULL,
  `timeout` bigint(20) DEFAULT NULL,
  `userid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pwrecoverykey` (`pwrecoverykey`),
  KEY `FK9C4F969C13C75A0C` (`userid`),
  CONSTRAINT `FK9C4F969C13C75A0C` FOREIGN KEY (`userid`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `server_properties`;
CREATE TABLE `server_properties` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `propertyname` varchar(255) NOT NULL,
  `propertyvalue` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `propertyname` (`propertyname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `stamp`;
CREATE TABLE `stamp` (
  `stamptype` varchar(16) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `itemid` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `itemid` (`itemid`,`stamptype`),
  KEY `idx_stamptype` (`stamptype`),
  CONSTRAINT `FK68AC3C3FF55C69C` FOREIGN KEY (`itemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `subscription`;
CREATE TABLE `subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  
  `target_collection_id` bigint(20) NOT NULL,
  `ownerid` bigint(20) ,
  `ticketid` bigint(20) NOT NULL,
  `proxy_collection_id` bigint(20) DEFAULT NULL,
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `ownerid_target_collection` (`ownerid`,`target_collection_id`),
  UNIQUE KEY `ownerid_proxy_collection` (`ownerid`,`proxy_collection_id`),
  CONSTRAINT `FK_TARGET_COLLECTION_ID` FOREIGN KEY (`target_collection_id`) REFERENCES `item` (`id`),
  CONSTRAINT `FK_OWNERID` FOREIGN KEY (`ownerid`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_TICKETID` FOREIGN KEY (`ticketid`) REFERENCES `tickets` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_PROXY_COLLECTION_ID` FOREIGN KEY (`proxy_collection_id`) REFERENCES `item` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `ticket_privilege`;
CREATE TABLE `ticket_privilege` (
  `ticketid` bigint(20) NOT NULL,
  `privilege` varchar(255) NOT NULL,
  PRIMARY KEY (`ticketid`,`privilege`),
  CONSTRAINT `FKE492FD3EC068F18E` FOREIGN KEY (`ticketid`) REFERENCES `tickets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `creationdate` datetime DEFAULT NULL,
  `ticketkey` varchar(255) NOT NULL,
  `tickettimeout` varchar(255) NOT NULL,
  `itemid` bigint(20) DEFAULT NULL,
  `ownerid` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ticketkey` (`ticketkey`),
  KEY `FKB124F6E7FF55C69C` (`itemid`),
  KEY `FKB124F6E76BE46F4` (`ownerid`),
  CONSTRAINT `FKB124F6E76BE46F4` FOREIGN KEY (`ownerid`) REFERENCES `users` (`id`),
  CONSTRAINT `FKB124F6E7FF55C69C` FOREIGN KEY (`itemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `tombstones`;
CREATE TABLE `tombstones` (
  `tombstonetype` varchar(16) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `removedate` bigint(20) NOT NULL,
  `stamptype` varchar(255) DEFAULT NULL,
  `localname` varchar(255) DEFAULT NULL,
  `namespace` varchar(255) DEFAULT NULL,
  `itemuid` varchar(255) DEFAULT NULL,
  `itemid` bigint(20) NOT NULL,
  `itemname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK40CA41FEFF55C69C` (`itemid`),
  CONSTRAINT `FK40CA41FEFF55C69C` FOREIGN KEY (`itemid`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `user_preferences`;
CREATE TABLE `user_preferences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `preferencename` varchar(255) NOT NULL,
  `preferencevalue` varchar(255) NOT NULL,
  `userid` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `userid` (`userid`,`preferencename`),
  CONSTRAINT `FK199BD08413C75A0C` FOREIGN KEY (`userid`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `createdate` bigint(20) DEFAULT NULL,
  `etag` varchar(255) DEFAULT NULL,
  `modifydate` bigint(20) DEFAULT NULL,
  `activationid` varchar(255) DEFAULT NULL,
  `admin` tinyint(4) DEFAULT NULL,
  `email` varchar(128) DEFAULT NULL,
  `firstname` varchar(128) DEFAULT NULL,
  `lastname` varchar(128) DEFAULT NULL,
  `locked` tinyint(4) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `uid` varchar(255) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_activationid` (`activationid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO users (id, createdate, etag, modifydate, activationid, admin, email, firstname, lastname, locked, password, uid, username) 
	VALUES(NULL, 1404983699604, 'Y1PlUVPg/qxugBgLtXoQC9u8k8M=', 1404983699604, null, 1, 'root@localhost', 'Cosmo', 'Administrator', 0, md5('cosmo'), '648e2565-2081-4e60-9cac-306a4ffb8d64', 'root');

SET FOREIGN_KEY_CHECKS = 1;