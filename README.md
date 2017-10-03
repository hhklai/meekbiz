#### Meekbiz.com ####
Meekbiz.com is a web platform for micro businesses.   The idea is that people can post a test business such as "I will Chinese cuisine for you at my place".   Meekbiz will try to connect people in the Meekbiz community test subjects for the business so that valuable feedback can be given to determine if the business idea is viable.  Meekbiz will continue to support the growing business by providing billing services, location scheduling services, and messaging services.

#### Environment Set up #### 

#### Here are some tools you will need #### 

MySQL Community Server (Use InnoDB)
Java JDK 1.7+
Play Framework 2.2
NodeJS (for frontend testing)
Elastic search 1.7+ (download the bin, no need to install)

#### Steps to set up environment #### 

Install all the tools above, and their dependencies. 

Log into MySQL and run the sql file from the repository path /sql/rebuildMeekbizDb.sql
Start Elastic search, bin/elasticsearch in the elastic search download package, go to http://localhost:9200 to check it is running. This step is important when we seed the search tool with dev data a few steps down.
Open a command window where you checked out the code. Run the following command
activator run
The server should be up and running. Go to http://localhost:9000 to see if it runs. Apply the sql evolution if prompted. This needs to be done before the next step so that play can create the table structure from the evolution scripts.

Log into MySQL and run the sql file from the repository path /sql/devData.sql to seed dev data into the db. The seed data will update search on the next start up of the server, so if you need the data now, restart the play server.
Test account credentials are:
Regular user: test / test1234
Admin: admin / admin1234
Moderator: moderator / moderator

#### Steps to set up tests #### 

Play framework tests can be run by using the following command (make sure elastic search is running while you run the test)
activator test
For angular tests the following steps are needed to set up nodejs and karma test runner
Install karma after installing nodejs by running the following
npm install -g karma
npm install -g karma-cli
npm install -g karma-jasmine
npm install -g karma-requirejs
npm install -g karma-chrome-launcher
Run the angular tests by going to the folder /test/angular/conf . Inside the folder run the following command
karma start