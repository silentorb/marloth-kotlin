# Holistic Compositional RPG Systems

## Background

* The fun of a game is heavily dependent upon player engagement
* One of the best means of engaging a player is through challenge
* Challenges work best when they are a configuration of composable elements
  * This allows for a ratio of bang-for-your-buck
  * This allows the players to be both familiar with the elements and faced with a fresh challenge

## The Problem

### Dungeons and Dragons

* D&D is composed of many systems
* These systems are non-orthogonal
* These systems allow for minimal composition

### The Example of Lock Picking

* D&D contains a lock-picking system
* Thieves can pick locks
* Lock picking has little relationship with the rest of the game
  * It has no direct impact on combat, the primary system of the game
    * Nor does combat have any direct impact on lock picking
    * Rarely does a thief pick a lock in the middle of a battle
  * The actual act of lock picking is simply a dice roll
    * Some implementations provide mini-games that may add a little more challenge but that challenge is still disconnected from the rest of the game
  * As far as character creation goes, there is a threshold where either a character is good at lock picking and you pick most any lock you come across, or your character is bad at lock picking and you don't try to pick locks
    * This minimizes any challenge that might be posed by deciding whether to pick a lock
      * Lock picking is rarely a dilemma
* In practice, lock picking is never compositional
  * Players are rarely faced with an array of distinctly locked objects and the choice as to which lock to pick
  * Lock picking is a solo activity
  * Lock picking has no relation to simultaneous lock picking attempts by other characters
    * More than one lock is rarely picked at the same time
  * Other than some implementations requiring a pick and picks sometimes breaking, short term game state has little effect on lock picking

### Problem Summary

* Generally role playing games fall under one of two categories
  1. A primary combat system supported by a secondary character development system
  2. A plethora of independent systems supported by a thin resolution system
* I want a role playing game that is a holistic plethora of deeply integrated systems

## The Solution

* How to integrate all of these systems together?

### Requirements

* The systems and integrations need to
  * Be sensible and relatable
    * Though only to a point—this *is* a surreal fantasy
  * Be practical to develop and manage
  * Maximize composition *across systems*
  * Be easy for players to learn, remember, and reason about
  * Be easy to communicate via UI and for players to interact with
* For a real-time 3D game, spatial reality is the most universal system that other system integrations need to utilize and comply with

### Framework

* Such an endeavor will probably need a common framework and language that each system is based on
* One of the most compositional game frameworks is the card game, where nearly every element of the game is a card
  * Marloth isn't a card game, but may be able to maximize any potential similarities

### Narrative Support

* I want systems that maximize rich narrative
* Most of those systems involve characters and character relationships

### Possible Systems

* Combat
  * Wars
* Spiritual
  * Maintenance
  * Charity
  * Corruption
    * Blatant
    * Hidden
* Home
* Travel
  * Exploration
* Security
  * Walls
  * Doors
  * Locks
  * Keys
  * Lock Picks
* Stealth
* Medical
* Factions
  * Leaders
  * Spies
* Family
* Captivity
  * Rescue
* Economy
* Employment
  * Hiring
  * Firing
  * Resigning
* Industry
* Gardening
* Government
  * Law
  * Crime
* Mystery
  * Investigation
* Misinformation
  * Lying
  * Misunderstanding
  * Confusion
* Catastrophes
* Science
  * Research
    * New Technology
* Magic
* Arts and Entertainment
* History

### Iterations

* Above is an ambitious list of systems, but the key for any of this to work is for just a few of these systems to be engaging and worth the development cost
* The greatest challenge of creating such a system is to use ingredients that work well by themselves yet also harmonize with the other ingredients