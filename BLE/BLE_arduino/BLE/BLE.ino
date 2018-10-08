/*
	Name:       BLE.ino
	Created:	4.10.2018 12.10.34
	Author:     DESKTOP-PT6PB57\Aleksi
*/

#include <SPI.h>
#include <BLEPeripheral.h>

#define BUTTON_PIN 4
#define STATUS_LED 3

#define BLE_REQ 6
#define BLE_RDY 2
#define BLE_RST 9

#define BLE_NAME "Bluetooth Laite"
#define BLE_SER "AAA0"
#define BLE_CHAR "AAA1"
#define BLE_DESC_NUM "2901"
#define BLE_DESC_VAL "ARVO"

BLEPeripheral BLE = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

int buttonState;
int lastButtonState = LOW;

unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;

float lastReading;
unsigned long long  lastSent = 0;

BLEService service = BLEService(BLE_SER); //https://www.bluetooth.com/specifications/gatt/services
//BLEFloatCharacteristic characteristic = BLEFloatCharacteristic(BLE_CHAR, BLERead | BLEWrite | BLEWriteWithoutResponse | BLENotify); https://www.bluetooth.com/specifications/gatt/characteristics
BLEUnsignedShortCharacteristic characteristic = BLEUnsignedShortCharacteristic(BLE_CHAR, BLERead | BLEWrite | BLEWriteWithoutResponse | BLENotify /*| BLEIndicate*/);
BLEDescriptor descriptor = BLEDescriptor(BLE_DESC_NUM, BLE_DESC_VAL); //https://www.bluetooth.com/specifications/gatt/descriptors

volatile int toggle = 0;
volatile unsigned int counter = 0;

void setup() {
	Serial.begin(9600);
	Serial.println("Software start");

    pinMode(BUTTON_PIN, INPUT_PULLUP);

    pinMode(STATUS_LED, OUTPUT);
    digitalWrite(STATUS_LED, LOW);

	BLE.setLocalName(BLE_NAME);
    BLE.setDeviceName(BLE_NAME);
    BLE.setAdvertisedServiceUuid(service.uuid());
    BLE.setAppearance(0x0080);

	BLE.addAttribute(service);	
    BLE.addAttribute(characteristic);
	BLE.addAttribute(descriptor);

	BLE.setEventHandler(BLEConnected, BLEConnectHandler);
	BLE.setEventHandler(BLEDisconnected, BLEDisconnectHandler);

    characteristic.setEventHandler(BLEWritten, characteristicWritten);
    characteristic.setEventHandler(BLESubscribed, characteristicSubscribed);
    characteristic.setEventHandler(BLEUnsubscribed, characteristicUnsubscribed);

    characteristic.setValue(0);

	BLE.begin();

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

            if (millis() > 1000 && (millis() - 1000) > lastSent) {
                // atleast one second has passed since last increment
                lastSent = millis();

                // increment characteristic value
                //characteristic.setValue(characteristic.value() + 1);

                Serial.print(F("counter = "));
                Serial.println(characteristic.value(), DEC);
            }
     
            int reading = digitalRead(BUTTON_PIN);

            if (reading != lastButtonState) {
                lastDebounceTime = millis();
            }

            if ((millis() - lastDebounceTime) > debounceDelay) {
                if (reading != buttonState) {
                    buttonState = reading;
                    if (buttonState == HIGH) {
                        setCharacteristicValue();
                    }
                }
            }

            lastButtonState = reading;
        }

        // central disconnected
        Serial.print(F("Disconnected from central: "));
        Serial.println(central.address());
    }
}

void setCharacteristicValue() {
    int reading = rand();

    characteristic.setValue(reading);

    Serial.print(F("Temperature: ")); Serial.print(reading); Serial.println(F("C"));
    lastReading = reading;
}

void BLEConnectHandler(BLECentral& central) {
	Serial.print(F("Connected: "));
	Serial.println(central.address());

    //digitalWrite(STATUS_LED, HIGH);
}

void BLEDisconnectHandler(BLECentral& central) {
	Serial.print(F("Disconnected: "));
	Serial.println(central.address());

    //digitalWrite(STATUS_LED, LOW);
}

void characteristicWritten(BLECentral& central, BLECharacteristic& chara) {
    // characteristic value written event handler

    Serial.print(F("Characteristic event, writen: "));
    Serial.println(characteristic.value(), DEC);
    counter = characteristic.value();
}


void characteristicSubscribed(BLECentral& central, BLECharacteristic& characteristic) {
    // characteristic subscribed event handler
    Serial.println(F("Characteristic event, subscribed"));
}

void characteristicUnsubscribed(BLECentral& central, BLECharacteristic& characteristic) {
    // characteristic unsubscribed event handler
    Serial.println(F("Characteristic event, unsubscribed"));
}

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

