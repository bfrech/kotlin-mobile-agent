# kotlin-mobile-agent


```
{
  "@context":["https://www.w3.org/ns/did/v1"],
  "id":"did:peer:1zQmWpAKgFhugQoFmuxmVkKsjPiG7JRvYxtDkBLCSd3Jht7E",
  "verificationMethod":[
    {
      "controller":"#id",
      "id":"did:key:z6MkofRrFv7NyuRSYLtNEdfskEh9gdt419RLnchcFyhgxaCa",
      "publicKeyBase58":"ADAoffrweMvyRr3fZ4i2u999s4cCbGAz6bngRhjg3MRC",
      "type":"Ed25519VerificationKey2018"
    },
   {
      "controller":"",
      "id":"did:key:z6LSjwLNdvvr826QwwCTrtioXJ7WcZhAQJDY1qQ5FTThqz7E",
      "publicKeyBase58":"UhJiX5sjN2Q716rsieAxQc9i87ffSXDnjpQybNu....byjGqUCjGrVbquA13Y3ESrbnk",
      "type":"X25519KeyAgreementKey2019"
    }
 ],
 "service":[
    {
      "accept":["didcomm/v2"],
      "id":"c6605ca6-a11d-4a14-9e3b-79500d1d7a54",
      "priority":0,
      "recipientKeys":["did:key:z6LSjwLNdvvr826QwwCTrtioXJ7WcZhAQJDY1qQ5FTThqz7E"],
      "routingKeys":["did:key:z6LScEErxQesFbMMvRRuMzTEYxwfThhoSrPDtCaSxzyB5WrU"],
      "serviceEndpoint":{
        "uri":"ws://MBP-von-Berit.fritz.box:5001",
        "routingKeys":["did:key:z6LScEErxQesFbMMvRRuMzTEYxwfThhoSrPDtCaSxzyB5WrU"]
      },
      "type":"DIDCommMessaging"
  }
],
  "authentication":["did:key:z6MkofRrFv7NyuRSYLtNEdfskEh9gdt419RLnchcFyhgxaCa"],
  "assertionMethod":["did:key:z6MkofRrFv7NyuRSYLtNEdfskEh9gdt419RLnchcFyhgxaCa"],
  "keyAgreement":["did:key:z6LSjwLNdvvr826QwwCTrtioXJ7WcZhAQJDY1qQ5FTThqz7E"],
  "created":"2022-06-07T15:33:24.708001629Z",
  "updated":"2022-06-07T15:33:24.708001629Z"
 }

```



```
{
  "id": "1234567890",
  "type": "<message-type-uri>",
  "from": "did:example:alice",
  "to": ["did:example:bob"],
  "created_time": 1516269022,
  "expires_time": 1516385931,
  "body": {
    "message": "Hello",
    "goal": "I just want to say hello"
  }
}

```


```
{
    "type": "https://didcomm.org/routing/2.0/forward",
    "id": "12345678",
    "to": ["did:peer:mediator"],
    "expires_time": 1516385931,
    "body":{
        "next": "did:peer:Bob"
    },
    "attachments": [
        // The payload to be forwarded
    ]
}

```


```
{
  "@type": "https://didcomm.org/connection/2.0/message",
  "purpose": "connection-request",
  "body": {
		"did_doc": "<Base64 encoded DID document>",
    "label": "Alice"
  }
}
```
