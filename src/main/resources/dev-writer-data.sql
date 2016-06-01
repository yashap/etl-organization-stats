CREATE TABLE IF NOT EXISTS "organization_statistic" (
  "organization_id" int(11) NOT NULL,
  "day" date NOT NULL,
  "statistic" varchar(100) NOT NULL,
  "value" decimal(11,2) NOT NULL,
  PRIMARY KEY ("organization_id", "day", "statistic")
);
