# General Game Design

## Core Gameplay

What is the core gameplay of Marloth.  Every good game I've ever played has had a simple core.  A core that is still marginally fun even if all the other layers of content and depth were stripped away.  This game has yet to have such a core.

## Micro / Macro

Good games have layers.  Choices contained within encounters contained within chapters contained within campaigns.

## Growth

Growth in games is fun.  It gives players a heightened sense of accomplishment, and it can be used to morph game mechanics over the course of a game session.

Growth is most fun when it is unbridled, except that it is hard to maintain balance with unbridled growth.  If balance is lost then the game loses much of its fun.

Unbridled growth creates a growth curve.  The growth curve outlines the median rate of growth a player can progress along which will maintain a balanced challenge.  If the player gets ahead of the curve the game loses its challenge.  If the player falls behind the curve the game becomes more challenging to the point of impossibility.

What's more, a growth curve is exponential.  Riches beget more riches and debts beget more debts.  If the player can get slightly ahead of the curve early on, that will grant them an edge to get even further ahead later.  Conversely, falling behind a little means increasing missed opportunities later.

Most games do not have unbridled growth.  Most games use one or more methods to restrict growth.  The primary methods for restricting growth are:

### Grinding

Rewarding the player for labor instead of skill.  I don't like grinding and want to avoid it in any game I make with one exception, grinding can sometimes be a useful *fallback* as long as its possible to bypass the grinding with enough skill.

### Check point limits

This only works for games that are broken up into stages.  Each stage has a limit to how much a character can grow.  This is a better method than grinding but still greatly cheapens the sense of growth in the game.

### Fixed growth

Taking "Check point limits" a step further, some games have fixed growth, in that the character is hard-coded to grow in specific ways at different stages of the game.  This method is especially common in FPS campaigns, where at different stages the player will be given new and better weapons as part of the story.  Acquiring the new weapon often is not even an option, in which case the player cannot progress through the story until the weapon is picked up.

### Scaling loss

This is less common but also one of the more interesting and healthy approaches.  It plays on the notion of "the bigger they are the harder they fall."  With this method, if a player gets ahead of the growth curve, he loses far more if he fails in any way, while someone below the growth curve will struggle more, but has less to lose.

### Auto-scaling enemies
If the player does really well, the game increases the challenge.  This is a cheap option and is rarely employed.

### Diminishing returns

By itself this does not bridle growth, but can help soften the effects of players getting ahead of the growth curve.

### Ceilings

Diminishing returns and ceilings are basically the same principle, just different math.  This is different from check point limits in that this is a global ceiling, independent of what stage of the game the player is at.  It is next to impossible to balance a game with no ceilings.  Having no cap can cause infinite, unpredictable problems.  Not only does it provide logistical problems, but it also provides technical problems, in that a computer is a finite machine and while a game could officially support a player character reaching level 6 trillion, that wouldn't be practical.

### No growth feedback loop
Basically, if a game avoids rewarding growth with more growth.  Implementing this is actually standard for most genres except strategy games.  

### Shortcuts

### Some challenges not effected by growth

One classic case of this is platform games with role playing elements.  The game may allow upgrading your weapons to make it easier to fight enemies, but doesn't feature any upgrades to make a jumping sequence any easier.

### Fake growth / micro growth

## Items and Abilities

#### Intrinsic differences between Items and Abilities

* Abilities are generally more permanent than items
* Items are usually stored within a finite collection of inventory and equipment slots
* Ability acquisition is usually not limited by quantity
* Abilities are more personal than items

#### Extraneous differences between Items and Abilities

* Items are often acquired through loot and merchants.  Abilities normally aren't
* Abilities are usually gained through leveling and class-centric ability trees
* Items are rarely gained through leveling
* Items are more often tied to cosmetics
  * This is primarily due to items occupying singular equipment slots, removing most concerns for conflicting cosmetic elements

#### Ability Pros

* When not constrained by leveling, having room for unlimited abilities can be a lot of fun

#### Ability Cons

* When not constrained by leveling, having room for unlimited abilities can cause balance problems
  * This is sometimes the case with The Binding of Isaac
    * Occasionally offset by some abilities overriding previous abilities
      * This mechanic is unpredictable and makes for bad UX

## Leveling

#### Pros

* Provides a high-level metric for comparing silentorb.mythic.characters
  * An algorithm for rating silentorb.mythic.characters could be used instead
    * While such an algorithm may be involved to create and maintain, a leveling system can be involved as well
* Gives an added sense of growth and accomplishment
* Simplifies balancing character growth
  * Reduces character growth balance to balancing the rate of leveling, and ensuring that the advantages gained each level are balanced.  This results in two normalized dimensions.
* Grinding is heavily dependent on leveling
  * There are other forms of grinding, such as for item drops, though that is usually secondary.
* Leveling can be used to gate character growth
  * For example, D&D campaigns sometimes Character levels
  * Can be used to prevent dilution across dimensions
    * Most RPGs with a shared stash does this by placing minimum character levels on items

#### Cons

* Most of the sense of accomplishment is a a cheap illusion
  * Arguably all of this is an illusion, leveling is just a little cheaper
    * You could easily make a game with many different dimensions of leveling, but this is not generally done because it doesn't increase the fun
* Makes character growth more rigid and less organic
* Experience gaining can be too general and result in a disconnect between your actions and your character growth
  * Experience is a universal currency.  Performing different tasks does not result in different types of experience rewards, just different amounts of experience rewarded.  Quantity is the only distinguishing attribute for experience acquisition.
    * In other words, experience is not distinct
    * Separate types of experience can be used, but that generally requires having separate tracks of leveling, which is essentially diluting leveling

Note that normally the pros and cons of leveling are diminished by the accompaniment of additional dimensions.  Rarely is leveling the only dimension of character growth in an RPG.  The most common second dimension is item acquisition.  This is probably the strongest argument against leveling: Leveling is often so diluted by additional dimensions that its impact on the game is minimal, resulting in a feature that adds little value.

## Character Classes

### Major Character Classes

Character classes that have significant aspects of the game directly tied to them

#### Pros

* Helps create distinct personalities and themes
* Its partitioning of abilities can help with game balance
  * This can prevent imbalanced ability combinations
* Allows the player to shape the style of a playthrough up front

#### Cons

* Restricts character design
  * Character design becomes much more a matter of following templates
* Frontloads much of character designing
  * Choosing your character class is the single most defining character design decision 
  * Choosing an initial class is not reactive
* Class design options are rarely affected by environment
  * This does not have to be the case but is the standard

### Minor Character Classes

Character classes that start the game with a few unique abilities that color the rest of the playthrough

#### Pros

* Has none of the weaknesses of major character classes

#### Cons

* Becomes irrelevant later in the game
* Does not provide much personality or theme
* Does not prevent imbalanced ability combinations

### Trivial Character Classes

Character classes that are simply starting configurations

#### Pros

* Has none of the weaknesses of major character classes
* Least amount of development effort
* Best compromise between class and classless.
  * In other words, if you're leaning toward classless, this is probably you're best choice

#### Cons

* Becomes irrelevant later in the game
* Does not provide much personality or theme
* Does not prevent imbalanced ability combinations

### Multi-Major Character Classes

Allowing the option to select two or three classes.  Usually the availability of adding a class is stair stepped.

#### Pros

* Feels more creative than being required to use a single class
  * With this method, even when a player chooses to only use one class it still feels more creative because the player is *choosing* to specify.
* Allows for a wider variety of ability combinations
* Character class selection becomes both a means to define the style of playthrough up front, but also a way to react to circumstances later in the game

#### Cons

* Allows for more imbalanced ability combinations
  * This tends to result in slightly more generic abilities to ensure balanced cross-pollination
  * Grim Dawn minimizes this slightly by providing Ultimate abilities, only one of which can be active at a time
* Can dilute themes
  * However, a player can still choose to only use a single class, thus maintaining a purer theme

## Races

Races are a more specialized form of character class.  Usually if an RPG has races, it also has classes, forming a matrix of character creation combinations.

