/*
Anatomy – downregulates – Gene				AdG
Anatomy – expresses – Gene					AeG
Anatomy - upregulates – Gene				AuG
Disease - downregulates - Gene				DdG
Disease - associates - Gene					DaG
Disease - upregulates - Gene				DuG
Disease - localizes - Anatomy				DlA
Disease - resembles - Disease				DrD
Gene - participates - Biological Process	GpBP
Gene - participates - Cellular Component	GpCC
Gene - participates - Molecular Function	GpMF
Gene - participates - Pathway				GpPW

Anatomy -> Gene
("AdG", "AeG", "AuG")

Disease -> Gene
("DdG", "DaG", "DuG")

Disease -> Anatomy
"DlA"

Disease -> Disease
"DrD"

Gene -> {Biological Process, Cellular Component, Molecular Function, Pathway}
("GpBP", "GpCC", "GpMF", "GpPw")
*/

# Sample from Anatomy -> Gene. Row count = 726504
select count(*) from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG");
select id, source, target, metaedge from
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG")) as ag;

# Sample from Disease -> Gene. Row count = 27986
select count(*) from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG");
select id, source, target, metaedge from
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG")) as dg;

# Sample from Disease -> Anatomy. Row count = 3605
select count(*) from edgeAutoFull where metaedge = "DlA";
select id, source, target, metaedge from
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA") as da;

# Sample from Disease -> Disease. Row count = 546
select count(*) from edgeAutoFull where metaedge = "DrD";
select id, source, target, metaedge from
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd;

# Sample from Gene -> {Biological Process, Cellular Component, Molecular Function, Pathway}. Row count = 814676
select count(*) from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw");
select id, source, target, metaedge from
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") as gb;

# Create a 1k sample of edges.
create table edge_1k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 200) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 200) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA" limit 200) as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD" limit 200) as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 200) as gb;

# Create a 2k sample of edges.
create table edge_2k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 400) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 400) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA" limit 400) as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD" limit 400) as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 400) as gb;

# Create a 4k sample of edges.
create table edge_4k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 1054) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 800) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA" limit 800) as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 800) as gb;

# Create a 8k sample of edges.
create table edge_8k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 2054) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 1800) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA" limit 1800) as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 1800) as gb;

# Create a 16k sample of edges.
create table edge_16k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 4249) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 3800) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA") as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 3800) as gb;

# Create a 32k sample of edges.
create table edge_32k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 9583) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 9133) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA") as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 9133) as gb;

# Create a 64k sample of edges.
create table edge_64k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 20249) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG") limit 19800) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA") as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 19800) as gb;

# Create a 128k sample of edges.
create table edge_128k as
select id, source, target, metaedge from # Row count = 726504
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("AdG", "AeG", "AuG") limit 47931) as ag
union
select id, source, target, metaedge from # Row count = 27986
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("DdG", "DaG", "DuG")) as dg
union
select id, source, target, metaedge from # Row count = 3605
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DlA") as da
union
select id, source, target, metaedge from # Row count = 546
(select id, source, target, metaedge from edgeAutoFull where metaedge = "DrD") as dd
union
select id, source, target, metaedge from # Row count = 814676
(select id, source, target, metaedge from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw") limit 47932) as gb;


select metaedge, count(*) from edgeAutoFull where metaedge in ("GpBP", "GpCC", "GpMF", "GpPw")
group by metaedge;
