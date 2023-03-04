#include <ESP8266WiFi.h>
#include <Wire.h>
#include "MAX30100_PulseOximeter.h"
#include "FirebaseESP8266.h"

#define FIREBASE_HOST "heartrate-69b79-default-rtdb.firebaseio.com"  //Database link
#define FIREBASE_AUTH "xDq4c4AFpYuXLRKj1wvuw8expn6YqW9orf2VNJUH"  //Database secrate

#define WIFI_SSID "IOT TRAFFIC"      //Router name
#define WIFI_PASSWORD "12345678"  //Router password

FirebaseData firebaseData;
#define REPORTING_PERIOD_MS     1000

const double VCC = 3.3;             // NodeMCU on board 3.3v vcc
const double R2 = 10000;            // 10k ohm series resistor
const double adc_resolution = 1023; // 10-bit adc

const double A = 0.001129148;   // thermistor equation parameters
const double B = 0.000234125;
const double C = 0.0000000876741;


PulseOximeter pox;
uint32_t tsLastReport = 0;
bool found=false;

void onBeatDetected() {
  Serial.println("♥ Beat!");
  found=true;
}



void setVal(String path, String v) {
  Firebase.setString(firebaseData, "/Data/" + path, v);
}



void setup() {
  Serial.begin(9600);




  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");

  while (WiFi.status() != WL_CONNECTED)
  {
    digitalWrite(D4, 0);
    Serial.print(".");
    delay(200);
    digitalWrite(D4, 1);
    Serial.print(".");
    delay(200);

  }

  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  Firebase.setString(firebaseData, "/Testing", "OK!");;
  delay(500);
  
  Serial.print("Initializing pulse oximeter..");

  // Initialize sensor
  if (!pox.begin()) {
    Serial.println("FAILED");
    for (;;);
  } else {
    Serial.println("SUCCESS");
  }
  pox.setIRLedCurrent(MAX30100_LED_CURR_7_6MA);
  pox.setOnBeatDetectedCallback(onBeatDetected);
}

void loop() {

    double Vout, Rth, temperature, adc_value; 
    pox.update();

    if (millis() - tsLastReport > REPORTING_PERIOD_MS) {

        if(found){
            
            pox.shutdown();
//            Serial.print("Heart rate:");
//            Serial.print(pox.getHeartRate());
//            Serial.print("bpm / SpO2:");
//            Serial.print(pox.getSpO2());
//            Serial.print("%  |  "); 
            found=false;

            adc_value = analogRead(A0);
            Vout = (adc_value * VCC) / adc_resolution;
            Rth = (VCC * R2 / Vout) - R2;
          
            temperature = (1 / (A + (B * log(Rth)) + (C * pow((log(Rth)),3))));   // Temperature in kelvin
          
            temperature = temperature - 273.15;  
//          Serial.print("Temperature = ");
//          Serial.print(temperature);
//          Serial.println(" degree celsius");
            String out=String(pox.getHeartRate()+30)+"!"+String(pox.getSpO2())+"!"+String(temperature);
            //Serial.println(out);
            setVal("Report", out);
            pox.resume();
            tsLastReport = millis();
        }

    }
}
