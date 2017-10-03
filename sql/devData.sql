INSERT INTO `meekbizdb`.`user`
(`id`,
 `name`,
 `email`,
 `password`,
 `profilePic`,
 `isAdmin`,
 `isModerator`)
VALUES
  ('6eca1828-1200-b1e9-d894-014f7d3a828b', 'admin', 'admin@meekbiz.com', '$2a$12$QIasZf9EpLAX2FDODwOy6eu/./JxJiGnuVM0fxz7cseFN7SJye4DS', NULL, true, false),  /* password is admin1234 */
  ('6eca1829-1200-b1e9-d894-014f7d3b2fe3', 'moderator', 'moderator@meekbiz.com', '$2a$12$KvPqUFSjS6fSCcNVKiCCjOx6DErxJ.tVHjs1lVMyemMuF7ZL2dDuG', NULL, false, true), /* password is moderator */
  ('6b8be333-1150-b1e9-d894-014f33143cf7', 'test' , 'test@example.com', '$2a$12$JoZGiCc0vmycOsN2nW/qMufhW67I6ibYLs44WE/3/jednS9hKaEM2', NULL, false, false),        /* password is test1234 */
  ('6b8be33d-1150-b1e9-d894-014f33d50689', 'test1', 'test1@example.com', '$2a$12$2.cym51OiJEJMFSGfMkW7.0Dhz9lwKXZXhpHOrE3d/IXCJL9orsJa', NULL, false, false),       /* password is test1234 */
  ('6b8be342-1150-b1e9-d894-014f344740d6', 'test2', 'test2@example.com', '$2a$12$4/lqMfL1ZE/WHl5KuLycCOGX9Bk9qcxPtyQN5haIJdP4urD9oaZ/y', NULL, false, false),       /* password is test1234 */
  ('6b8be347-1150-b1e9-d894-014f344b062d', 'test3', 'test3@example.com', '$2a$12$O01gXQRE9nFKZ4SPBGaLfeQ1pfgxMevX08Gu8BV500LAfejyl33tm', NULL, false, false),       /* password is test1234 */
  ('6b8be34c-1150-b1e9-d894-014f344e409c', 'test4', 'test4@example.com', '$2a$12$4Vl.FW6MvyvBCKlhyIhQa.GEeb6NWrANhGoyLQjcFjSvhfgnxL.OW', NULL, false, false),       /* password is test1234 */
  ('6b8be351-1150-b1e9-d894-014f3451f53f', 'test5', 'test5@example.com', '$2a$12$ctsqc0RB21zVlM.OFHwUI.aqRcGxwlm3EBXT8IH9/E90.CIQFlO4C', NULL, false, false),       /* password is test1234 */
  ('6b8be356-1150-b1e9-d894-014f34550d54', 'test6', 'test6@example.com', '$2a$12$9sfybS25Fd6lzXk0wohRdeIyV/JC.iFfFQOx8zxoTG0CZeYte9kVO', NULL, false, false);       /* password is test1234 */


INSERT INTO `meekbizdb`.`miz`
(`id`,
`ownerId`,
`editable`,
`isPublic`,
`urlTitle`,
`initialModeratedBy`,
`initialModerationDate`,
`highQuality`,
`bidStartDate`,
`bidEndDate`,
`serviceValidStartDate`,
`serviceExpiryDate`,
`locationEnum`,
`locationRadius`,
`locationLat`,
`locationLng`,
`locationRegion`,
`minCustomers`,
`maxCustomers`,
`minPricePerCustomer`,
`valuePropositionEnum`,
`searchSynched`)
VALUES
('6b8be334-1150-b1e9-d894-014f331635a7', '6b8be333-1150-b1e9-d894-014f33143cf7', '1', '1', 'Beer Making Experience With Sam McGee', NULL, '2015-08-15 14:38:57', '0', '2015-08-15 14:38:57', '2015-08-15 14:38:57', '2015-08-15 14:38:57', '2015-08-15 14:38:57', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be33e-1150-b1e9-d894-014f33d56fa1', '6b8be33d-1150-b1e9-d894-014f33d50689', '1', '1', 'Wine Tasting Experience With Samuel McJefferies', NULL, '2015-08-15 18:07:49', '0', '2015-08-15 18:07:49', '2015-08-15 18:07:49', '2015-08-15 18:07:49', '2015-08-15 18:07:49', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be343-1150-b1e9-d894-014f3447b0f8', '6b8be342-1150-b1e9-d894-014f344740d6', '1', '1', 'Noble Gardens Farm Tour', NULL, '2015-08-15 20:12:37', '0', '2015-08-15 20:12:37', '2015-08-15 20:12:37', '2015-08-15 20:12:37', '2015-08-15 20:12:37', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be348-1150-b1e9-d894-014f344c9b36', '6b8be347-1150-b1e9-d894-014f344b062d', '1', '1', 'Guitar Lessons', NULL, '2015-08-15 20:17:59', '0', '2015-08-15 20:17:59', '2015-08-15 20:17:59', '2015-08-15 20:17:59', '2015-08-15 20:17:59', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be34d-1150-b1e9-d894-014f344ff746', '6b8be34c-1150-b1e9-d894-014f344e409c', '1', '1', 'Chinese Cuisine Experience', NULL, '2015-08-15 20:21:40', '0', '2015-08-15 20:21:40', '2015-08-15 20:21:40', '2015-08-15 20:21:40', '2015-08-15 20:21:40', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be352-1150-b1e9-d894-014f34524bcd', '6b8be351-1150-b1e9-d894-014f3451f53f', '1', '1', 'Hair Styling', NULL, '2015-08-15 20:24:12', '0', '2015-08-15 20:24:12', '2015-08-15 20:24:12', '2015-08-15 20:24:12', '2015-08-15 20:24:12', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0'),
('6b8be357-1150-b1e9-d894-014f34555844', '6b8be356-1150-b1e9-d894-014f34550d54', '1', '1', 'At Home Fitness Trainer', NULL, '2015-08-15 20:27:32', '0', '2015-08-15 20:27:32', '2015-08-15 20:27:32', '2015-08-15 20:27:32', '2015-08-15 20:27:32', '1', NULL, NULL, NULL, 'Calgary', '0', '20', '0', '0', '0');

INSERT INTO `meekbizdb`.`s3file`
(`id`,
 `publicId`,
 `ownerId`)
VALUES
  ('6b8be336-1150-b1e9-d894-014f3318083e', '14823ab2-0aab-46da-acde-32310bfdf737', '6b8be333-1150-b1e9-d894-014f33143cf7'), /*http://besthomebreweryguide.com/wp-content/uploads/2014/08/Home-Brew-setup-2.jpg*/
  ('6b8be33c-1150-b1e9-d894-014f33bec17f', 'ea20fe8b-dd9e-479b-af4b-978d57a2d5ac', '6b8be333-1150-b1e9-d894-014f33143cf7'), /*http://besthomebreweryguide.com/wp-content/uploads/2014/08/Home-Brew-setup-2.jpg*/
  ('6b8be340-1150-b1e9-d894-014f343f1419', '00a765ae-6a51-47cc-81d5-408c4e2f5f92', '6b8be33d-1150-b1e9-d894-014f33d50689'), /*https://www.cfpwinemakers.com/uploads/winemaking-fresh-juice_001.jpg*/
  ('6b8be341-1150-b1e9-d894-014f343f5377', '6c7cf818-3edf-4f3b-bdf6-e68ab883f811', '6b8be33d-1150-b1e9-d894-014f33d50689'), /*https://www.cfpwinemakers.com/uploads/winemaking-fresh-juice_001.jpg*/
  ('6b8be345-1150-b1e9-d894-014f344977af', '6a3d74de-8cc1-4d5c-ba74-636b5dc1edcf', '6b8be342-1150-b1e9-d894-014f344740d6'), /*http://www.noblegardenscsa.com/uploads/6/4/6/6/6466645/1001503973_orig.jpg*/
  ('6b8be346-1150-b1e9-d894-014f3449e54d', 'ab0ba0ce-3e8b-4f14-8fb0-daaea12fcac9', '6b8be342-1150-b1e9-d894-014f344740d6'), /*http://www.noblegardenscsa.com/uploads/6/4/6/6/6466645/1001503973_orig.jpg*/
  ('6b8be34a-1150-b1e9-d894-014f344d94d3', 'a25c7193-28ca-462f-9833-7db3e8ab101b', '6b8be347-1150-b1e9-d894-014f344b062d'), /*http://www.lovethyneighbormusic.com/wp-content/uploads/2014/12/online-guitar-lessons.jpg*/
  ('6b8be34b-1150-b1e9-d894-014f344dc2a6', 'cf8f0b14-5209-44bf-be38-83922140ac0b', '6b8be347-1150-b1e9-d894-014f344b062d'), /*http://www.lovethyneighbormusic.com/wp-content/uploads/2014/12/online-guitar-lessons.jpg*/
  ('6b8be34f-1150-b1e9-d894-014f34510df2', '5c711b5a-3e03-4fc3-b670-b1eafa8f1bcb', '6b8be34c-1150-b1e9-d894-014f344e409c'), /*http://www.citizensforchoice.com/wp-content/uploads/2015/06/chinesefood-050913-sb-tif.jpg*/
  ('6b8be350-1150-b1e9-d894-014f34517695', 'a32f114c-e035-4678-ae84-28215912cf55', '6b8be34c-1150-b1e9-d894-014f344e409c'), /*http://www.citizensforchoice.com/wp-content/uploads/2015/06/chinesefood-050913-sb-tif.jpg*/
  ('6b8be354-1150-b1e9-d894-014f34531384', '1673e566-b331-47db-99f7-79524c1c874e', '6b8be351-1150-b1e9-d894-014f3451f53f'), /*http://www.hairandmakeupideas.com/wp-content/uploads/2015/06/hair-styling-4.jpg*/
  ('6b8be355-1150-b1e9-d894-014f34535439', 'a9d0b2ec-844c-42e1-992a-89a049dbaf5e', '6b8be351-1150-b1e9-d894-014f3451f53f'), /*http://www.hairandmakeupideas.com/wp-content/uploads/2015/06/hair-styling-4.jpg*/
  ('6b8be359-1150-b1e9-d894-014f3456ec4d', '4d1326e7-2f2e-4e56-9ec4-31bdef3a8aec', '6b8be356-1150-b1e9-d894-014f34550d54'), /*http://media.sanluisobispo.com/smedia/2015/02/22/16/22/gE4rc.AuSt.76.jpeg*/
  ('6b8be35a-1150-b1e9-d894-014f34575fb9', 'b587ba44-ac05-4afc-8c89-6e98bebf0c11', '6b8be356-1150-b1e9-d894-014f34550d54'); /*http://media.sanluisobispo.com/smedia/2015/02/22/16/22/gE4rc.AuSt.76.jpeg*/

INSERT INTO `meekbizdb`.`mizcontent`
(`id`,
 `mizId`,
 `moderatedBy`,
 `createdDate`,
 `title`,
 `summary`,
 `approxServiceDurationEnum`,
 `bannerPic`,
 `thumbnail`,
 `contentBody`)
VALUES
  ('6b8be335-1150-b1e9-d894-014f331635af', '6b8be334-1150-b1e9-d894-014f331635a7', NULL, '2015-08-15 14:38:57',
   'Beer Making Experience With Sam McGee',
   'Learn to make make beer with Sam McGee.  Sam has 10 years experience brewing his own beer and has perfected his recipe through many trials and tribulations.',
   '0', 'ea20fe8b-dd9e-479b-af4b-978d57a2d5ac', '14823ab2-0aab-46da-acde-32310bfdf737',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Join me for a 2 hour tutorial on making beer. &nbsp;This is a one time event where I will demonstrate how to make my secret blend. &nbsp;Lessons include hands on training using the boiling pot and fermenter. &nbsp;Each guest will get to bring home a sample of Sam\'s homebrew beer, as well as their own creation. &nbsp;Perfect for dates. &nbsp;Bring the whole family. &nbsp;Children under 18 will not be served alcoholic beverages, but can still take part in the beer making process. &nbsp;Class sizes are limited to under 10 people. &nbsp;Classes will start only if minimum is reached.</p>'),
  ('6b8be33f-1150-b1e9-d894-014f33d56fa4', '6b8be33e-1150-b1e9-d894-014f33d56fa1', NULL, '2015-08-15 18:07:49',
   'Wine Tasting Experience With Samuel McJefferies',
   'Learn to the intricacies of wine with Samuel McJefferies.  Samuel has 10 years experience as a somelier.  The tour will give an overview of the wine making process, and let you sample 20 different wines ranging from different regions of the world.  Wine pairing appetizers are included.',
   '0', '00a765ae-6a51-47cc-81d5-408c4e2f5f92', '6c7cf818-3edf-4f3b-bdf6-e68ab883f811',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Join me for a 2 hour experienc on wine tasting. &nbsp;This is a one time event where I will give a tour of my private wine collection, and sample different wines from around the world. &nbsp;Each sample will come with a food pairing to demonstrate the full depth and complexity of each wine. &nbsp;Class sizes are limited to under 10 people. Classes will start only if minimum is reached.</p>'),
  ('6b8be344-1150-b1e9-d894-014f3447b0fb', '6b8be343-1150-b1e9-d894-014f3447b0f8', NULL, '2015-08-15 20:12:37',
   'Noble Gardens Farm Tour', 'Noble farms is a local family run farm that provides organic, hand grown vegetables.   The farm has been in the family for seven generations.  Tour includes tutorial on the farming process, and hands on training to operate the irigation system.  Guests will receive a bag of goodies including vegetables for the week.', '0', '6a3d74de-8cc1-4d5c-ba74-636b5dc1edcf', 'ab0ba0ce-3e8b-4f14-8fb0-daaea12fcac9', '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Join us for a 2 hour experience on growing vegetables. &nbsp;This is a one time event where the family farm will be made public. &nbsp;Learn how everyday vegetables are grown, and the special care required in order to grow vegetables to meet organic certification.</p>'),
  ('6b8be349-1150-b1e9-d894-014f344c9b38', '6b8be348-1150-b1e9-d894-014f344c9b36', NULL, '2015-08-15 20:17:59',
   'Guitar Lessons', 'Geoff Williams is an aspiring musician, and truly passionate about his art.  He has opened for bands such Black Eye Jacks.  He has over 100 gigs in the city.  Join him for free guitar lessons.  Beginners welcomed.',
   '0', 'a25c7193-28ca-462f-9833-7db3e8ab101b', 'cf8f0b14-5209-44bf-be38-83922140ac0b',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Free private guitar lessons. &nbsp;Geoff wants to spread his art to as many people as possible. &nbsp;Minimum 10 people must sign up before it is available for all.</p>'),
  ('6b8be34e-1150-b1e9-d894-014f344ff748', '6b8be34d-1150-b1e9-d894-014f344ff746', NULL, '2015-08-15 20:21:40',
   'Chinese Cuisine Experience', 'Tired of cooking at home?  Try an in home cooked meal by Cindy Chan.  Cindy has many years of experience cooking for her kids.  Patrons will get a choice between 3 different menu\'s:  Szechaun Saucy Porky Chops, Cindy\'s Special Fried Rice, or Chow Mein with Fresh Meat.',
   '0', '5c711b5a-3e03-4fc3-b670-b1eafa8f1bcb', 'a32f114c-e035-4678-ae84-28215912cf55',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Meals are offered at Cindy\'s House and are only available on weekdays dinner time. &nbsp;Please notify Cindy of any allergies. &nbsp;Because Cindy is testing out whether this idea can become a restaurant, she will only provide this service.</p>'),
  ('6b8be353-1150-b1e9-d894-014f34524bcf', '6b8be352-1150-b1e9-d894-014f34524bcd', NULL, '2015-08-15 20:24:12',
   'Hair Styling', 'Jennifer has previously styled hair at First Choice Haircutters and Great Clips.  She has decided to break out on her own and needs your support.  Great deals on haircuts to the first 10 supporters.',
   '0', '1673e566-b331-47db-99f7-79524c1c874e', 'a9d0b2ec-844c-42e1-992a-89a049dbaf5e',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>Jennifer will cut your hair at your home. &nbsp;She has all the necessary equipment (except broom and vacuum) and will clean up after cutting your hair.</p>'),
  ('6b8be358-1150-b1e9-d894-014f34555845', '6b8be357-1150-b1e9-d894-014f34555844', NULL, '2015-08-15 20:27:32',
   'At Home Fitness Trainer', 'Laina is a 3rd kineseology student at the University of Calgary.  She maintains a strict regiment and healthy lifestyle and can help you achieve it too! Please help her achieve her goal of starting a business in fitness by supporting her first Meekbiz Event!',
   '0', '4d1326e7-2f2e-4e56-9ec4-31bdef3a8aec', 'b587ba44-ac05-4afc-8c89-6e98bebf0c11',
   '<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n                      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n<p>At home fitness training. &nbsp;Laina has all the training equipment required. &nbsp;Please have your own yoga mat. The routine consists of cardio and core exercises. &nbsp;Resistance bands and light weights will be used. &nbsp;Each training session is 45 minutes and can be purchased in packages of 20.</p>');
