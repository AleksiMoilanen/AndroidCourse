// Visual Micro is in vMicro>General>Tutorial Mode
// 
/*
	Name:       BLE.ino
	Created:	4.10.2018 12.10.34
	Author:     DESKTOP-PT6PB57\Aleksi
*/

#include <SPI.h>
#include <BLEPeripheral.h>

#define BLE_REQ 6
#define BLE_RDY 2
#define BLE_RST 9

#define BLE_NAME "Mörkö"
#define BLE_SER "AAA0"
#define BLE_CHAR "AAA1"
#define BLE_DESC_NUM "2901"
#define BLE_DESC_VAL "ARVO"

BLEPeripheral BLE = BLEPeripheral(BLE_REQ, BLE_RDY, BLE_RST);

BLEService service = BLEService(BLE_SER); //https://www.bluetooth.com/specifications/gatt/services
BLEFloatCharacteristic characteristic = BLEFloatCharacteristic(BLE_CHAR, BLERead | BLENotify); //https://www.bluetooth.com/specifications/gatt/characteristics
BLEDescriptor descriptor = BLEDescriptor(BLE_DESC_NUM, BLE_DESC_VAL); //https://www.bluetooth.com/specifications/gatt/descriptors

void setup() {
	Serial.begin(115200);
	Serial.println("Software start");

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
}

void BLEConnectHandler(BLECentral& central) {
	Serial.print(F("Connected: "));
	Serial.println(central.address());
}

void BLEDisconnectHandler(BLECentral& central) {
	Serial.print(F("Disconnected: "));
	Serial.println(central.address());
}