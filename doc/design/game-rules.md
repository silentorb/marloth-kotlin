# Game Rules

## Character Resources

### Overview

#### List

Each character has the following resources:
* Health / Injury
* Food / Starvation
* Sanity / Madness
* Energy / Exhaustion

Each resource waxes / wanes over time

#### Limits

* If a character's Injury or Starvation reach 100% that character dies
* If a character's Energy reaches 100% that character is rendered unconscious until woken

* If a character's Madness reaches 100% that character becomes an out of control monster
  * This state could be called "Beyond Sane"

* If either Injury, Starvation or Madness reach 100% for a player, the game is over
  * Multiplayer may still have respawning
    * Not sure how respawning would work for 100% Madness

#### Perspective

* When a player's sanity is greater than or equal to 50%, resources are described in positive terms

* When a player's sanity is below 50%, resources are described in negative terms.

### Details

#### Injury

* Injuries are mostly gained through combat
* Injuries can also be gained through fall damage
  * Possibly also by getting crushed between heavy collision objects?
* Injuries

#### Starvation

#### Madness

* As the player's madness increases, worse monsters and calamities are unleashed upon the denizens of the world
* The effects are a metaphor for and reflection of the player character's own wickedness
* Spiritually edifying activities reduce madness

#### Exhaustion

* May have a dream dimension that the player can interact with while sleeping

## Social Systems

* Characters should be able to initiate dialog with other characters
  * Top priority for this is for players to be able to initiate dialog with AI players
  * Ideally eventually every combination of player and AI character dialog initiation should be supported
    * Players initiating dialog with AI
    * AI initiating dialog with AI
    * AI initiating dialog with players
    * Players initiating dialog with players
* Dialog consists of selecting from a menu of messages to communicate
  * The messages available to a character are dependent on that character's state and relationship with the other party
  * There should be a global limit to the amount of options available per dialog choice
    * 4 might be a good number
    * Options will be weighted based on circumstances so when the option limit is reached, higher weighted options take precedence
* Dialog options fall into two categories:
  * Initiation options
  * Response options
* Some dialog options end the dialog
  * There should be an indication in the GUI to distinguish such options
* Players can exit a dialog at any time
  * When doing so, the player character will speak an automatic message such as "Sorry, got to go!"
  * Manually exiting dialog prematurely should affect game state
    * For example, this should annoy the other party and potentially disable certain dialog options
* Players can sometimes be automatically forced out of dialog
  * Possibly the most common occurrence of this will be getting attacked by third-parties
  * Exiting dialog in this manner should not adversely affect your standing or dialog options with the other party
  * Characters should be able to resume a dialog that is forcefully cancelled
    * The game should be designed to streamline this flow with automatic flavor messages such as "Anyhow, so where were we?"
* The availability of some dialog options should be determined by certain skills that can be acquired by a character
  * A single skill can unlock multiple dialog options
  * Skills can have multiple levels
    * Having a low level of a skill may not be enough to enable certain dialog options in certain circumstances
* There could possibly be a skill similar to the D&D Charisma stat that would boost the strength of multiple dialog enabling skills
* Dialog should be very purposeful
  * This system is not about rewarding the player for talking to every NPC and seeing what dialog options are available
  * The player should be mildly punished for indiscriminate dialog initiation
  * With a little experience, players should have some idea of what to expect before initiating dialog
  * The dialog system should be similar to casting targeted spells on other characters in a combat system
    * When entering combat in a game, the player knows what spells they are equipped with
      * Some spells will be more or less effective when applied to different characters
        * But most of the time most spells have some use
* The Dialog system should inform the player of the quantified factors being applied to dialog
  * Some of these values should be viewable before dialog is initiated
    * Similar to how CRPGs will usually show at least the numeric value of an enemy's health, and sometimes much more information such as attack damage and armor rating

