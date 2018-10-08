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

BLEService service = BLEService(BLE_SER); //https://www.bluetooth.com/specifications/gatt/services
BLEFloatCharacteristic characteristic = BLEFloatCharacteristic(BLE_CHAR, BLERead | BLENotify); //https://www.bluetooth.com/specifications/gatt/characteristics
BLEDescriptor descriptor = BLEDescriptor(BLE_DESC_NUM, BLE_DESC_VAL); //https://www.bluetooth.com/specifications/gatt/descriptors

void setup() {
	Serial.begin(115200);
	Serial.println("Software start");

    pinMode(BUTTON_PIN, INPUT_PULLUP);

    pinMode(STATUS_LED, OUTPUT);
    digitalWrite(STATUS_LED, LOW);

	BLE.setLocalName(BLE_NAME);

	BLE.setAdvertisedServiceUuid(service.uuid());
	BLE.addAttribute(service);	
    BLE.addAttribute(characteristic);
	BLE.addAttribute(descriptor);

	BLE.setEventHandler(BLEConnected, BLEConnectHandler);
	BLE.setEventHandler(BLEDisconnected, BLEDisconnectHandler);

	BLE.begin();

	characteristic.setValue(100);
}

void loop() {
	BLE.poll();

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

void setCharacteristicValue() {
    float reading = 100.0;

    //if (!isnan(reading) && significantChange(lastReading, reading, 0.5)) {
        characteristic.setValue(reading);

        Serial.print(F("Temperature: ")); Serial.print(reading); Serial.println(F("C"));

        lastReading = reading;

    //}
}

boolean significantChange(float val1, float val2, float threshold) {
    return (abs(val1 - val2) >= threshold);
}

void BLEConnectHandler(BLECentral& central) {
	Serial.print(F("Connected: "));
	Serial.println(central.address());

    digitalWrite(STATUS_LED, HIGH);
}

void BLEDisconnectHandler(BLECentral& central) {
	Serial.print(F("Disconnected: "));
	Serial.println(central.address());

    digitalWrite(STATUS_LED, LOW);
}