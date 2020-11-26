# Strategy

## Strategy in Video Games

* When it comes to standard video game genre definitions, the term "strategy game" is so overly narrow it is a misnomer
* A strategy game does not need
  * Symmetrical opposing forces
  * An overhead view
  * Soldier management
* Strategy is about planning ahead
* What a game needs to be a strategy game is to inform the player to a minimal degree about the future, and reward the player for planning for that future
* Informing players about what lies ahead is a mechanic largely non-existent in games
  * Either
    * a) The game presents the player with all immediate game state and contains just enough variables to make prediction challenging
    * b) Future game state is opaque but predefined and the player has to play multiple times to know what lies ahead
    * c) Future game state is random and the player cannot know what lies ahead

## Strategy in Marloth

* The elusive design goal I've been pursuing for years has been for a game where
  * The future is random
  * The player is significantly informed of that future
  * The primary challenge is for the player to plan for that future
* One of the primary challenges is how to inform the player
  * It is tempting to rely on specific mechanics
    * That does not work well
    * It will result in brittle and non-orthogonal systems
    * Some specific information systems can be added, but a general system is needed first and foremost
  * The informing system should be based on real life strategy
    * Real life strategy is dependent on general, high level conclusions that inform specific decisions
    * However, those general conclusions can be induced through aggregating specific data
* 
  In summary, the core system should not rely on specifics directly informing specifics, but instead specific data informing a general conclusion which in turn informs specific actions
  * A one-two punch of induction and deduction
* 