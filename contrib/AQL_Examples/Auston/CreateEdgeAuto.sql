insert into nodeKindAuto (kind)
select kind from nodeKind;

select * from nodeKindAuto;

select source, target, metaedge, count(*) from edge
group by source, target, metaedge
having count(*) > 1;

CREATE TABLE `edgeAuto` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `source` varchar(255) DEFAULT NULL,
  `metaedge` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_edgea_source` (`source`),
  KEY `idx_edgea_target` (`target`),
  KEY `fk_metaedgea_idx` (`metaedge`),
  CONSTRAINT `fk_edgea_source_node` FOREIGN KEY (`source`) REFERENCES `node` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_edgea_target_node` FOREIGN KEY (`target`) REFERENCES `node` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_metaedgea` FOREIGN KEY (`metaedge`) REFERENCES `edgeKind` (`metaedge`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into edgeAuto (`source`, `metaedge`, `target`)
select `source`, `metaedge`, `target` from sample_edge;

CREATE TABLE `edgeAutoFull` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `source` varchar(255) DEFAULT NULL,
  `metaedge` varchar(255) DEFAULT NULL,
  `target` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_edgeaf_source` (`source`),
  KEY `idx_edgeaf_target` (`target`),
  KEY `fk_metaedgeaf_idx` (`metaedge`),
  CONSTRAINT `fk_edgeaf_source_node` FOREIGN KEY (`source`) REFERENCES `node` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_edgeaf_target_node` FOREIGN KEY (`target`) REFERENCES `node` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_metaedgeaf` FOREIGN KEY (`metaedge`) REFERENCES `edgeKind` (`metaedge`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into edgeAutoFull (`source`, `metaedge`, `target`)
select `source`, `metaedge`, `target` from edge;

select kind, count(*) from nodeKind
group by kind
having count(*) > 1;

select metaedge, count(*) from edgeKind
group by metaedge
having count(*) > 1;

select id, count(*) from sample_node
group by id
having count(*) > 1;

select concat('e', id), count(*) from edgeAuto
group by id
having count(*) > 1;

select count(*) from node;