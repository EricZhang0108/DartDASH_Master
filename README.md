# CS65 Final Project
## Sean Hawkins and Eric Zhang, May 2018

# DartDASH

DartDASH is an Android mobile application dedicated to helping Dartmouth organizations with fundraising using student's DASH account. The clubs can simply use the OCR technology to scan the student's ID to get the name and DASH number of each customer instead of having to write all of them down manually. After that, The customer can sign on the phone itself to approve of the transactions. All of that information will be saved conveniently in a list under each event. The organization can then export an event's transaction history to Excel spreadsheet to be emailed and saved. Students can also use DartDASH to see ongoing events around campus and the organizer's email to contact with. DartDASH provides a much faster way to charge the DASH account and make fundraising easier and more enjoyable. This is the final project for Smartphone Programming (CS65). Please see our website for more information:
https://ericzhang0108.github.io/DartDASH/#

## Getting Started

### Prerequisites

Android Studio or ADB
Android Phone Oreo 8.0 or above

### Installing

Load up Android studio to build and run on the phone. Alternatively, you can compile this as an APK and use ADB to run it on the phone.

## Running the tests

To test, simply follow the installation instructions and test it on the phone.

### And coding style tests

The coding style follows standard Java and XML conventions.

## Deployment

It is abled to be deployed on Android Phones Oreo 8.0 or above.

## Built With
Java, SQLite, XML, Android Studio, Google APIs, Signature Pad, opencsv, and Apache

## Authors

* **Sean Hawkins**
* **Eric Zhang**

## Acknowledgments

* Special thanks to the course CS65 materials and helpful Google documentations
* Also thanks to to the template from Colorlib, it can be found here:
https://colorlib.com/wp/template/eatwell/
* Thanks to the Dartmouth Baker Library Photo 
https://www.dartmouth.edu/~library/bakerberry/general/AboutBaker-BerryLibrary.html
* Thanks to the Signature Pad API from Cacace
https://github.com/gcacace/android-signaturepad
* Thanks to Apache POI API
https://poi.apache.org/
* Thanks to opencsv
http://opencsv.sourceforge.net/


### Implementation

The implementation of the app's UI is largely done through XML involving widgets, menus, and layouts. When the user first launches the application's MainActivity which redirect the user to LoginActivity if the user is not logged in already. The authentication of the application is handled by Firebase which requires an email and password to be registered. This account is then tied in with the user's real-time database that makes up the back-end of the application.

After logging in, the user goes back to the MainActivity. Here there are two fragments the user has access to: NewEvent Fragment and EventHistoryFragment. In the NewEvent Fragment, the user can select the event's title and date (through a calendar dialogue). Then the user can proceed to launch that event that would help the user keep track of all the transactions for that event. The user can also navigate to OngoingActivity by clicking on that button underneath creating the new event. This allows the user to see all the ongoing events from all the users using the application. This is achieved through storing all the ongoing activities as "ongoing" in Firebase. When the user saves or deletes an event, it stops from being ongoing.

The user can also use the bottom navigation or swipe to go to the other EventHistory fragment. Here, the user can sync with firebase to get all the user's previous events and their transactions. This is loaded into a list view with the help of custom adapter and loader. The final thing to note is that there is a SettingsActivity that can be reached as well from the ActionBar. Here the user can select with the help of sharedPreferences dialogue and switch if they want the transactions to be exported with signatures, customize the thank you message, or sign out. 

In the OngoingActivity, it is also a list view with all the ongoing events listed. The user can also sync with Firebase to refresh the page. However, instead of money being displayed for each activity, it will just display the email of the organizer. This is also achieved through custom adapter.

In the EventActivity (Summary Fragment), it shows an event with the summery of all its transactions. The user can then click on the left camera button to use OCR to create a new transaction, or use the right bottom button to manually input (ConfirmationActivity) a new transaction. The OCR camera uses MLKit from the Firebase API that would convert the image taken to blocks of texts. The user should try to take a photo at a low angle for better results. The application would then try to decipher which are the names and DASH number from that. If it succeeds, it leads the user to the manual input with everything filled out. At the confirmation activity, it shows the customer's name, DASH, and amount being charged. Then if the user clicks on Sign, it will lead to the SignActivity. The SignActivity uses the Signature Pad API that would help the customer sign on the phone and save it in a local MySQLite database (when the event is saved) and be uploaded to Firebase (when the user clicks on sync in EventHistoryFragment). The phone would then display the custom Thank you message (WaitActivity) to signify the end of one transaction. The database is organized with the help of a datasource, helper, and Async Tasks to save and load. 

Going back to the EventActivity, the user can swipe away from SummaryFragment to TransactionHistoryFragment. This would show a list of all the transactions that have been recorded. The user can click on one to view more information such as the signature and can delete it through a button. After the conclusion of an event, the user can click on save to save the whole event into the SQLite database. The events and transactions are all stored with the help of the custom Transaction and Event Java classes.

All of these components help complete DartDASH to make it a fully functional app. The front end is made with XML with the back end is managed by SQLite and Firebase. Everything else is coded with Java with the help of some APIs. These are made into the activities mentioned above with other fragments that would help the users navigate and manage tasks in different threads. Overall, DartDASH would not have been possible without these and the helpful guidance from the TAs.



