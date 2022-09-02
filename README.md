# MOBIDID Mobile Client
This is the Android mobile client for mobile messaging using DIDComm. It can be used together with 
the [mediator](https://git.snet.tu-berlin.de/blockchain/idunion/DID) to send DIDComm messages with other agents. 

The project uses Aries-framework-go for all DIDComm interactions. 
The framework file is built from this [Fork](https://github.com/bfrech/aries-framework-go/tree/main) 
of the original framework. 



## Configuration and Run Instructions
The mobile client can be tested with a local mediator. To start the mediator locally, follow the 
instructions in the [mediator repository](https://git.snet.tu-berlin.de/blockchain/idunion/DID).

### Prerequisities
- AndroidStudio


### Run the Application on an Emulator



## Usage
On the startup screen the mediator URL and a label have to be entered. After a click on the 'Connect'
button, the app will create a new Aries agent and connect to the mediator. Note that an Android agent 
cannot fetch the URL via localhost when the server is run locally, hence ngrok should be configured 
first (see [run instructions](#Run the Application on an Emulator)). 


## Aries-framework-go
The project includes Aries-framework-go as a dependency for all DIDComm and Aries interactions. 
The framework file is built from the Fork of the repository at [https://github.com/bfrech/aries-framework-go/tree/main](). 

### How to build the framework file
To include changes to the framework code in the application, 

To build the framework file install [gomobile](https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile), 
and set the environment variables `ANDROID_HOME` and `ANDROID_NDK_HOME`.

Then run the following command in the `cmd/aries-agent-mobile` directory of the framework:
```
gomobile bind -v -ldflags '-s -w' -target=android/arm64,android/amd64 -javapkg=org.hyperledger.aries -o=./build/android/aries-agent.aar github.com/hyperledger/aries-framework-go/cmd/aries-agent-mobile/...
```
This creates the `aries-agent.aar` file that can be included in the [libs](app/libs) folder and 
referenced in the [build.gradle](app/build.gradle) file of the project.