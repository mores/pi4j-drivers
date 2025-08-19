
 Pi4J :: Drivers
==========================================================================

GitHub Actions: 
![Maven build](https://github.com/pi4j/pi4j-drivers/workflows/Build/badge.svg)

**THIS IS THE VERY START OF A NEW PROJECT. NO USABLE CODE IS YET AVAILABLE.**

This project contains driver implementations for various electronic components, using Pi4J V4+. Full description will be available on the Pi4J website at [Documentation > Using Drivers](https://pi4j.com/documentation/using-drivers).

## The Case for A Pi4j “driver” Subproject

Document created by **Stefan Haustein**.

### Background

Pi4J used to have a set of iot hardware drivers, but they have been removed from the main project due to maintenance issues. Currently, there is the [pi4j-example-devices](https://github.com/Pi4J/pi4j-example-devices/) subproject containing drivers.

The examples cover several common devices and show how to use Pi4J to communicate with devices, but they are not suitable for external projects to “just” depend on.

### Proposal

The proposal here is to insert a new subproject `pi4j-drivers`, so the dependency structure for `pi4j-example-devices` is as follows:

```text
pi4j
|
pi4j-drivers
|
pi4j-example-devices
```

Ideally, this would not lead to an expansion of code size. Instead, drivers that are converted by the `pi4j-drivers` subproject will be deleted in the example-devices project – keeping the examples in place.

Examples for the `pi4j-drivers` project will be contained in the `pi4j-example-devices` project.

The drivers project will allow users to use covered hardware in a straightforward way, taking advantage of modern build / dependency management to keep them up to date.

### Volunteers

* @eitch
  * Proper release management
* @stefanhaustein
  * Contribute a set of drivers (currently have [Bmx280Driver.java](https://github.com/stefanhaustein/tablecraft/blob/main/src/main/java/org/kobjects/pi4jdriver/sensor/bmx280/Bmx280Driver.java) and [Scd4xDriver.java](https://github.com/stefanhaustein/tablecraft/blob/main/src/main/java/org/kobjects/pi4jdriver/sensor/scd4x/Scd4xDriver.java) and can offer a PiXtendDriver if there is interest, but it’s a bit more “esoteric”; would also like to add a character lcd driver after some more cleanup). 
  * Would be willing to take part in ownership, i.e guide contributors / do code review
  * Would be willing to write PRs for removing redundant code from pi4j-example-devices
* @fdelporte
  * Documentation on the Pi4J website

### Policies / Properties

The purpose of the driver project is to provide an individual device-specific Java API for each covered device, abstracting from direct device communication (e.g via I2C or SPI).

The drivers will be grouped into packages such as `display`, `input, `actuator`, `sensor`,....

Ideally, all drivers are well-documented, feature complete wrt core functionality, high-quality and fully isolated – without any additional dependencies besides the pi4j project  (see examples above). There are no shared interfaces or helpers between drivers.

The reason for this isolation is to have a clear scope and simplify maintenance and to allow clients to review specifically the code they intend to depend on without overhead. The low-level IO abstraction is sufficient scope for a driver and additional abstractions can always be layered on top as needed.

Developers might not always have all devices available, so ideally, opportunities for changes in one driver affecting other drivers will be avoided from the start.

### No Shared Interfaces

**At least before we have a significant number of devices.**

Shared interfaces will make assumptions about what to expect from a device. Adding interfaces often seems straightforward initially, but as soon as a new device with additional capabilities comes along, the project will have to face a choice between a refactoring that can be significant, potentially affecting clients – or some form of incoherence. Trying to anticipate this will lead to overly complex interfaces, e.g. setPixel functions trying to cater to all imaginable color models.

Implementing shared interfaces might require additional logic that adds to the driver complexity where it could be a clean port of a reference implementation. It might make comparing the driver to a reference implementation more complex or lead to implementations that don’t expose functionality in a way that’s most “natural” for a given device.

Client code that intends to make devices exchangeable in any form can do so by adding a minimal adapter layer that maps a shared interface to each driver. Higher level convenience functionality should always go on top of such an additional layer, so it can be shared between devices and is not duplicated between drivers.

### No Shared Utilities

Shared utilities make it harder to review a driver in isolation. It’s easy to get into scope creep or misalignments. Also, as utilities will need to be accessible by all drivers, they will probably leak to users, making it hard to refactor them or clean them up, whereas local utilities can be limited to package visibility.

For instance, it might seem straightforward to add a utility for multibyte register access.

However, in a simple form, these seem to encourage “piecemeal” register access instead of bulk reads and writes / dynamic allocations. Considering a helper supporting bulk data, it seems to be largely redundant with nio.ByteBuffer.

Like for shared interfaces, this is not to say that shared utilities never will have a place. But it’s probably best to consider this at a later stage – and then to also consider if these wouldn’t be better placed in Pi4j core instead. After all, if these routines are useful for all drivers, they are probably useful for IoT code in general.

## Be Friendly – but Avoid Papering Over Issues

* If the current mode of the device can be tracked, and this can lead to better error messages, this probably makes sense.
* Avoid things that can lead to confusion / hiding errors – such as caching values obtained from the device.
* If the device has well-known timings, sleep accordingly before calls – if the user hasn’t done so already. Avoid sleeping after calls, set a “busyUntil” time instead. Expose this time so the user can schedule accordingly.

### Shared Drivers for Multiple Devices

For devices that are very similar, it might make sense to have a shared driver, in particular if they share a specification document or backwards compatibility is explicitly pointed out by the manufacturer (like the BME 280 spec describes compatibility to the BMP 280 in detail).

### Devices Supporting Multiple I/O Interfaces

There are various devices such as BME/BMP280 and BNO055 which support multiple interfaces.

These devices typically have some kind of I/O abstraction. For instance, they might use a register model, i.e. the device communication is specified in terms of register access, and then there is a dedicated part of the spec that defines how register access is modelled for each IO interface.

As Pi4J already provides a [register model abstraction](https://github.com/Pi4J/pi4j/blob/develop/pi4j-core/src/main/java/com/pi4j/io/i2c/I2CRegisterDataReaderWriter.java), the device driver can take this abstraction as input and provide implementations of this abstraction for each supported interface, as done for the [BMP280 driver example](https://github.com/stefanhaustein/tablecraft/blob/main/src/main/java/org/kobjects/pi4jdriver/sensor/bmx280/Bmx280Driver.java).

The same model will work as well for other communication abstractions that aren’t “predefined” in Pi4J. Note that this still doesn’t encourage interface sharing between different devices – typically this interface would be expected to be device specific and hence local.

#### Avoiding Subclassing

One might be tempted to implement an abstract driver instead, and then have interface specific subclasses. This has some disadvantages (cf [Composition over Inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance#Benefits)):

It makes it impossible to subclass the driver in another dimension. For instance, BOSCH might release a followup device with extended capabilities, where it would make sense to subclass the driver from the previous model. Subclassing for each interface would prevent this.

An abstract base driver mixes two “things” – the driver implementation and the IO abstraction – in one class. An IO abstraction is much easier to read and understand if it’s isolated in one interface.
There are cases where different IO modes are “add-ons”, for instance displays typically have a parallel hardware  interface that is then mapped to serial using a separate dedicated IO chip. Here, composition might offer better re-usability between drivers and a separation between the device command logic and form of transmission.

## BUILD DEPENDENCIES & INSTRUCTIONS

This project can be built with Maven.

## LICENSE

 Pi4J is licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
