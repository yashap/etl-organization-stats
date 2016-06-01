CREATE TABLE IF NOT EXISTS "organization" (
  "organization_id" int(11) NOT NULL,
  "organization_name" varchar(255) NOT NULL,
  "created_date" datetime NOT NULL,
  "modified_date" datetime NOT NULL,
  PRIMARY KEY ("organization_id")
);

MERGE INTO "organization" VALUES
  (1, 'Panama Farmers', '2011-01-21 12:03:34', '2011-01-26 00:48:23'),
  (2, 'Sublime', '2011-01-25 05:29:29', '2011-01-25 19:02:19')
;

CREATE TABLE IF NOT EXISTS "organization_user" (
  "organization_id" int(11) NOT NULL,
  "user_id" int(11) NOT NULL,
  "created_date" datetime NOT NULL,
  "modified_date" datetime NOT NULL,
  PRIMARY KEY ("organization_id", "user_id")
);

MERGE INTO "organization_user" VALUES
  (1, 1, '2013-01-21 12:03:34', '2013-01-26 00:48:23'),
  (1, 2, '2013-01-22 12:03:34', '2013-01-26 00:48:23'),
  (2, 3, '2014-01-25 05:29:29', '2014-01-25 19:02:19'),
  (2, 4, '2014-01-25 05:29:29', '2014-01-25 19:02:19')
;

CREATE TABLE IF NOT EXISTS "organization_event" (
  "id" char(100) NOT NULL,
  "timestamp" datetime NOT NULL,
  "organization_id" int(11) NOT NULL,
  "event" varchar(100) NOT NULL,
  PRIMARY KEY ("id")
);

MERGE INTO "organization_event" VALUES
  ('027a59f3-4cb2-4b29-a9ff-c138d1437c0e', '2016-04-12 01:34:32', 1, 'file_support_ticket'),
  ('0e9b0a14-51b9-45b0-8e9d-ba7bb991ecfb', '2016-04-12 15:02:54', 1, 'file_support_ticket'),
  ('02ae1954-ece8-4428-894b-03797ce026d6', '2016-04-10 07:45:05', 2, 'file_support_ticket'),
  ('07b6b4a0-bf42-495f-bd30-535cb0c9c725', '2016-04-10 18:50:24', 1, 'email_support'),
  ('09217cdd-8457-49a5-915e-b51c71d51c08', '2016-04-12 18:41:12', 2, 'email_support')
;

CREATE TABLE IF NOT EXISTS "organization_payment" (
  "id" char(100) NOT NULL,
  "timestamp" datetime NOT NULL,
  "organization_id" int(11) NOT NULL,
  "event" varchar(100) NOT NULL,
  "payment_amount" decimal(11,2) NOT NULL,
  "payment_processor" varchar(100) NOT NULL,
  PRIMARY KEY ("id")
);

MERGE INTO "organization_payment" VALUES
  ('0a93689b-3a26-40ca-bfe9-7a6c68bf4556', '2015-04-12 16:37:26', 373, 'paid_for_entperise_package', 459553.29, 'Chase'),
  ('0bcae327-f9e5-4c4a-b8f5-e6afb8536ee1', '2016-04-11 19:29:53', 171, 'paid_for_additional_team_members', 474384.23, 'PayPal'),
  ('0b7d6ec4-beac-4b7f-9d23-622ed7286848', '2016-04-12 15:00:49', 220, 'paid_for_additional_team_members', 66733.25, 'PayPal'),
  ('0281b7ba-1c47-4259-b84b-d3d82658c394', '2016-04-12 20:13:14', 170, 'paid_for_entperise_package', 123617.66, 'Chase'),
  ('0572fa3e-b64e-44b5-8a75-826bfce7c0e7', '2016-04-11 18:34:15', 481, 'paid_for_entperise_package', 206433.59, 'PayPal')
;

CREATE TABLE IF NOT EXISTS "user_event" (
  "id" char(100) NOT NULL,
  "timestamp" datetime NOT NULL,
  "user_id" int(11) NOT NULL,
  "event" varchar(100) NOT NULL,
  "social_network_type" varchar(100) DEFAULT NULL,
  PRIMARY KEY ("id")
);

MERGE INTO "user_event" VALUES
  ('0799d744-2f44-4330-a05d-d6c90a70ac43', '2016-04-10 02:36:34', 1, 'scroll_stream', 'Facebook'),
  ('09ac46ee-08c9-4183-9dbb-894601516de2', '2016-04-10 18:41:51', 2, 'add_stream', 'Twitter'),
  ('1045125a-9f35-488f-b406-09fe33aae0ce', '2016-04-11 05:30:18', 3, 'message_sent', 'Facebook'),
  ('12e55423-a1e4-4ad9-a891-c9a8a25121b9', '2016-04-12 16:16:24', 4, 'scroll_stream', 'Twitter'),
  ('1345d4ea-0303-4109-9891-027ed88eb94a', '2016-04-10 09:27:52', 1, 'scroll_stream', 'LinkedIn'),
  ('135ea46a-910f-40e6-8732-aa5abca1b91c', '2016-04-10 10:08:54', 2, 'message_sent', 'Twitter'),
  ('16904a39-11c0-4cff-ba1a-bb1ffef44e40', '2016-04-13 00:29:09', 3, 'scroll_stream', 'Instagram'),
  ('17578a04-41c9-4a0f-a13b-7a83c34f1fa1', '2016-04-10 11:25:23', 4, 'add_stream', 'LinkedIn'),
  ('1a090dd0-8c43-4703-8019-e417a2387cda', '2016-04-13 16:24:37', 1, 'message_sent', 'Instagram'),
  ('1f51fd35-1773-41a6-b2fa-87a3c05d9c35', '2016-04-12 06:49:43', 2, 'drop_stream', 'LinkedIn'),
  ('20955de1-7c0a-4c2e-a19c-dfea12ac07b8', '2016-04-10 23:45:42', 3, 'add_stream', 'Facebook'),
  ('231a6c59-be61-4424-ab23-561274b93165', '2016-04-12 02:43:50', 4, 'ios_logout', NULL),
  ('234f98cf-b54b-46f6-ba63-bc1bc0862867', '2016-04-11 20:46:45', 1, 'web_login', NULL),
  ('235cb63a-65ff-4726-9f2b-2c314e54f7dd', '2016-04-11 23:00:53', 2, 'web_logout', NULL),
  ('24d21a2f-870d-4f68-8e68-f36ee6346b65', '2016-04-10 15:28:53', 3, 'message_sent', 'LinkedIn'),
  ('253a2acc-c36d-4936-bccc-7db9e1d8b5ed', '2016-04-12 06:41:23', 4, 'ios_login', NULL),
  ('25c47b94-647f-4d52-96ac-bafb2f2853eb', '2016-04-11 02:56:53', 1, 'drop_stream', 'Facebook'),
  ('275e6409-3f43-4803-a2d2-2a6951107ea0', '2016-04-10 09:16:20', 2, 'android_logout', NULL),
  ('28c3ce77-0657-4ac4-93f5-cf18e2e341f0', '2016-04-12 17:51:30', 3, 'android_login', NULL),
  ('2990ecdb-2a25-4ca3-b793-2c8824582f9e', '2016-04-11 08:35:14', 4, 'drop_stream', 'Instagram'),
  ('3192868c-fc74-4e2e-98d1-53876d0b1a0f', '2016-04-10 07:03:31', 1, 'drop_stream', 'Twitter')
;
