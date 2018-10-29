/*
  Name:       BLE.ino
  Created:  4.10.2018 12.10.34
  Author:     DESKTOP-PT6PB57\Aleksi
*/

#include <SPI.h>
#include <BLEPeripheral.h>

#define BUTTON_PIN 4
#define STATUS_LED 10

#define LED_PIN 3

#define BLE_REQ 6
#define BLE_RDY 2
#define BLE_RST 9

#define BLE_NAME "Bluetooth Laite"
#define BLE_SER "AAA0"
#define BLE_CHAR "AAA1"
#define BLE_DESC_NUM "2901"
#define BLE_DESC_VAL "ARVO"

#define LED_SER "FFF0"
#define LED_CHAR "FFF1"
#define LED_DESC_NUM "2904"
#define LED_DESC_VAL "LED"


BLEPeripheral BLE = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

int buttonState;
int lastButtonState = LOW;

unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;

float lastReading;
unsigned long long  lastSent = 0;

boolean notifySub = false;

BLEService service = BLEService(BLE_SER); //https://www.bluetooth.com/specifications/gatt/services
//BLEFloatCharacteristic characteristic = BLEFloatCharacteristic(BLE_CHAR, BLERead | BLEWrite | BLEWriteWithoutResponse | BLENotify); https://www.bluetooth.com/specifications/gatt/characteristics
BLEUnsignedShortCharacteristic characteristic = BLEUnsignedShortCharacteristic(BLE_CHAR, BLERead | BLEWrite | BLEWriteWithoutResponse | BLENotify /*| BLEIndicate*/);
BLEDescriptor descriptor = BLEDescriptor(BLE_DESC_NUM, BLE_DESC_VAL); //https://www.bluetooth.com/specifications/gatt/descriptors

BLEService ledService = BLEService(LED_SER);
BLEIntCharacteristic ledCharacteristic = BLEIntCharacteristic(LED_CHAR, BLERead | BLEWrite);
BLEDescriptor ledDescriptor = BLEDescriptor(LED_DESC_NUM, LED_DESC_VAL);

volatile int toggle = 0;
volatile unsigned int counter = 0;

int ledValue = 0;
volatile int brightness = 0;

void setup() {
    Serial.begin(9600);
    Serial.println("Software start");
  
    pinMode(BUTTON_PIN, INPUT_PULLUP);
  
    pinMode(LED_PIN, OUTPUT);
    pinMode(STATUS_LED, OUTPUT);
  
    digitalWrite(STATUS_LED, LOW);
  
    BLE.setLocalName(BLE_NAME);
    BLE.setDeviceName(BLE_NAME);
  
    BLE.setAppearance(0x0080);
  
    BLE.setAdvertisedServiceUuid(service.uuid());
    BLE.addAttribute(service);
    BLE.addAttribute(characteristic);
    BLE.addAttribute(descriptor);
  
    BLE.setAdvertisedServiceUuid(ledService.uuid());
    BLE.addAttribute(ledService);
    BLE.addAttribute(ledCharacteristic);
    BLE.addAttribute(ledDescriptor);
  
    BLE.setEventHandler(BLEConnected, BLEConnectHandler);
    BLE.setEventHandler(BLEDisconnected, BLEDisconnectHandler);
  
    characteristic.setEventHandler(BLEWritten, characteristicWritten);
    characteristic.setEventHandler(BLESubscribed, characteristicSubscribed);
    characteristic.setEventHandler(BLEUnsubscribed, characteristicUnsubscribed);
  
    ledCharacteristic.setEventHandler(BLEWritten, ledCharacteristicWritten);
  
    characteristic.setValue(0);
    ledCharacteristic.setValue(0);
  
    BLE.begin();
  
    Serial.println("BLE ready");
    /*//set timer0 interrupt at 2kHz
    TCCR0A = 0;// set entire TCCR0A register to 0
    TCCR0B = 0;// same for TCCR0B
    TCNT0 = 0;//initialize counter value to 0
    // set compare match register for 2khz increments
    OCR0A = 124;// = (16*10^6) / (2000*64) - 1 (must be <256)
    // turn on CTC mode
    TCCR0A |= (1 << WGM01);
    // Set CS01 and CS00 bits for 64 prescaler
    TCCR0B |= (1 << CS01) | (1 << CS00);
    // enable timer compare interrupt
    TIMSK0 |= (1 << OCIE0A);*/
  
    //set timer1 interrupt at 4Hz
    TCCR1A = 0;// set entire TCCR1A register to 0
    TCCR1B = 0;// same for TCCR1B
    TCNT1 = 0;//initialize counter value to 0
    // set compare match register for 1hz increments
    OCR1A = 15624;// = (16*10^6) / (1*1024) - 1 (must be <65536)
    // turn on CTC mode
    TCCR1B |= (1 << WGM12);
    // Set CS10 and CS12 bits for 1024 prescaler
    //TCCR1B |= (1 << CS12) | (1 << CS10);  
    TCCR1B |= (1 << CS12);
    // enable timer compare interrupt
    TIMSK1 |= (1 << OCIE1A);
}

void loop() {
    BLECentral central = BLE.central();

    if (central) {
        // central connected to peripheral
        Serial.print(F("Connected to central: "));
        Serial.println(central.address());
    
        // reset counter value
        //characteristic.setValue(0);
    
        while (central.connected()) {
            // central still connected to peripheral
            if (characteristic.written()) {
                // central wrote new value to characteristic
                Serial.println(F("counter written, reset"));
      
                // reset counter value
                //lastSent = 0;
                //characteristic.setValue(0);
            }
        
            int reading = digitalRead(BUTTON_PIN);
        
            if (reading != lastButtonState) {
                lastDebounceTime = millis();
            }
        
            if ((millis() - lastDebounceTime) > debounceDelay) {
              
                if (reading != buttonState) {
                
                buttonState = reading;
                
                    if (buttonState == HIGH ){//&& notifySub == true) {
                      
                        setCharacteristicValue();
                    
                    }
                }
            }
        
            lastButtonState = reading;
        
            ledValue = ledCharacteristic.value();
            analogWrite(LED_PIN, ledValue);
            delay(10);
        }
    
        // central disconnected
        Serial.print(F("Disconnected from central: "));
        Serial.println(central.address());
    }
}

void setCharacteristicValue() {
    //int reading = rand();
    int reading = random(1, 5);
    //int reading = 1;

    characteristic.setValue(reading);

    Serial.print(F("New value: "));
    Serial.println(characteristic.value());
    lastReading = reading;
}

void BLEConnectHandler(BLECentral& central) {
    Serial.print(F("Connected: "));
    Serial.println(central.address());
}

void BLEDisconnectHandler(BLECentral& central) {
    Serial.print(F("Disconnected: "));
    Serial.println(central.address());
}

void characteristicWritten(BLECentral& central, BLECharacteristic& chara) {
    Serial.print(F("Characteristic event, writen: "));
    Serial.println(characteristic.value());
    counter = characteristic.value();
}

void ledCharacteristicWritten(BLECentral& central, BLECharacteristic& chara) {
    Serial.print(F("NEW LED VALUE: "));
    Serial.println(ledCharacteristic.value());
}


void characteristicSubscribed(BLECentral& central, BLECharacteristic& characteristic) {
    // characteristic subscribed event handler
    Serial.println(F("Characteristic event, subscribed"));
    notifySub = true;
}

void characteristicUnsubscribed(BLECentral& central, BLECharacteristic& characteristic) {
    // characteristic unsubscribed event handler
    Serial.println(F("Characteristic event, unsubscribed"));
    notifySub = false;
}   

/*
ISR(TIMER0_COMPA_vect) {


}*/

ISR(TIMER1_COMPA_vect) {
    if (counter > 0)
    {
        if (toggle)
        {
            digitalWrite(STATUS_LED, HIGH);
            toggle = 0;
        }
        else
        {
            digitalWrite(STATUS_LED, LOW);
            toggle = 1;
            counter--;
        }
    }
}
