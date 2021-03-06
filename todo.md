TODO List
=========

(Well, maybe more of an overview of the role of this library and others in a final game.)


Timestepping
* Better timestep handling options
* Processor with a fixed timestep, run it many times if it is left behind?  Would be ideal to run all processors and not just one though.
* [DONE] Support for manual timestepping
* [DONE] Timestep time might be moved to utils lib


Persistence
* Support for serializing the state of the simulation to a stream, and reloading it
* [DONE] Event support, player input expressed as events.
  * [DONE] Events added to queues in entities, handled by MessageHandlers
  * [DONE] Events could also be used for some inter-entity control / messaging, when a processor can not simply access both entities?
* Support for journaling serialization, save events and replay them from latest full backup to restore state.


Perception - best to put in separate game library that uses this library
* Relaying observations from their surrounding to characters, to be processed by AI or relayed to player client.
* Client / AI side representation of the perceived world, and things known about it from before.  As an EntitySystem, although with few processors (just client side motion prediction and the like)
  * Receives perception messages and updates the model


Configuration - this would be best to implement as separate library
* Support for reading configuration files in domain specific language from disk
  * Could simply be a javascript style object markup, possibly with expression language support, constants support, and some import mechanics.
    * Expressions could either be evaluated directly (for number valued or other fields), or parsed into expressions (for expression fields)
      * Create a separate library for the expression parsing & expression language?
        * Possible expression types: simple functional style expression, configuration language, or full language?
  * Use to create and initialize a set of specified beans
  * Possible to specify expressions to calculate bean properties, or to update the beans?
* Configuration editable on the fly in javabean style editor, listener interfaces for notifying the application


Networking - best to put in separate game library that uses this library
* Server side
  * Receive connections
  * Account / credentials handling
  * Character bootstrap
  * Control of own character(s) by sending messages
  * Send perceptions of world to client


Game specific code - separate project
* Game specific Components, Processors, etc.


Client - separate project
* Local perceived world model
* Appearance rendering
* Controls
* Base UI
* Controls and UI provided by various builtin and wielded components / entities of the player character
* Receive perceptions from server, relay commands
* Client side editors?


ControlInterface

Terrain
