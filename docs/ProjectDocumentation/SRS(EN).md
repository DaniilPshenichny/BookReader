# Requirements Document
### Contents
1. [Introduction](#1)
2. [User Requirements](#2) <br>
  2.1. [Software Interfaces](#2.1) <br>
  2.2. [User Interfaces](#2.2) <br>
  2.3. [User Characteristics](#2.3) <br>
3. [System Requirements](#3.) <br>
  3.1 [Functional Requirements](#3.1) <br>
  3.2 [Non-Functional Requierements](#3.2) <br>
  3.2.1. [Software Quality Attributes](#3.2.1) <br>
    3.2.1.1. [Usability](#3.2.1.1) <br>
    3.2.1.2. [Security](#3.2.1.2) <br>
  3.2.2. [External Interfaces](#3.2.2) <br>
  3.2.3. [Constraints](#3.2.3) <br>
4. [Analogues](#4) <br>

### Glossary
* FictionBook (also FeedBook) - the format for presenting electronic versions of books in the form of XML documents, where each element of the book is described by its own tags.
* Portable Document Format (PDF) is a cross-platform open electronic document format, originally developed by Adobe Systems using a variety of PostScript language features.
* EPUB is an open file format for electronic books developed by the International Digital Publishing Association (IDPF). The text, made in this format, automatically adapts to the screen size of a smartphone, laptop or e-book reader.

### 1\. Intoduction <a name="1"></a>
There are many programs for reading books under the Android system, but most of them significantly load the system due to the large consumption of resources, and this is perhaps one of the most important qualities.

And what if you create an application in which there will be no ads and which will be slightly loaded with various skins and add-ons, as well as reading all the popular formats of books? The answer to this question is just this project - BookReader.

### 2\. User Requirements <a name="2"></a>
#### 2.1\. Software Interfaces <a name="2.1"></a>
The project uses the Java programming language and does not interact with external systems and services.
#### 2.2\. User Interfaces <a name="2.2"></a>
The project’s graphical user interface is presented using mockups [Menu Bar(https://github.com/DaniilPshenichny/BookReader/blob/master/docs/ProjectDocumentation/Mockups/Menu%20bar.png), [The way to select a book](https://github.com/DaniilPshenichny/BookReader/blob/master/docs/ProjectDocumentation/Mockups/Book%20choice.png),[Function to share](https://github.com/DaniilPshenichny/BookReader/blob/master/docs/ProjectDocumentation /Mockups/Share%20bar.png) and [Example  of reading a book](https://github.com/DaniilPshenichny/BookReader/blob/master/docs/ProjectDocumentation/Mockups/Book%20reading.png).
Separate consideration is required by the menu tab:

Key | Reaction
--- | ---
"Current book" | The last book opened by the user opens.
"Library" | The window for selecting a book from the library opens.
"About app" | Developer Information Tab
"Night mode" | Slider to enable the night mode of the application
"Share" | Key to share information about the application

#### 2.3\. User Characteristics <a name="2.3"></a>
Target audience:
* Users who need a functional application for reading books under the Android system without ads.
#### 3\. System Requirements <a name="3"></a>
Run the application on the following operating systems:
* Android OS
#### 3.1\. Functional Requirements <a name="3.1"></a>
The user is provided with the features shown in the table.

Function | Requirements
--- | ---
Enabling the night mode of the application | The application should change the color when you click the slider "Night mode"
Adding books to the application library | On the "Library" tab, press the "+" key
The ability to share information about the application | The application should provide the opportunity to share information about it, when you click on the button "Share"

#### 3.2\. Non-Functional Requierements <a name="3.2"></a>
##### 3.2.1\. Software Quality Attributes <a name="3.2.1"></a>

#### 3.2.1.1 Usability Requirements
1. All elements must be bright;
2. All functional elements of the user interface have names that describe the action that will occur when an element is selected;
#### 3.2.1.2 Security Requirements
The application should work without mistakenly in the "Reading a book" tab;
### 3.2.2 External Interfaces
Application windows are convenient for use by visually impaired users:
  * font size at least 14pt;
  * Functional elements are contrasting with the window background.
### 3.2.3 Restrictions
 The application is implemented in the Java programming language;
### 4\. Analogues <a name="4"></a>
This project is a simplified version of ["Play Books"] (https://play.google.com/store/apps/details?id=com.google.android.apps.books&hl=en&gl=ru) and ["PocketBook"] (https://play.google.com/store/apps/details?id=com.obreey.reader&hl=en&gl=en).
