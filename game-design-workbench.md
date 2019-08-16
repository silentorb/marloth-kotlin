# Marloth Game Design Workbench

## Honing the game

### Genre

If I had to pick a single genre for Marloth, I'd have to say it would be RPG, and I'm kind of surprised by that.  Half of my inspirations have been non-RPGs, but those inspirations have primarily influenced setting, visual-style, and basic 3d mechanics.  In terms of gameplay, my primary influences have all been RPGs.  Part of the reason I have tried to avoid RPGs is because how they tend to be introverted in the way they focus on single character growth, but I am introverted, and I love single character growth.  It's one of my favorite parts about games.  Part of why I love it so much is because it's personal.  I am far more interested in the intrinsic abilities of a character than their equipment.

I'd also love to have as much strategy and simulation in this game as possible, but at the end of the day, RPGs have been far more staples than those other genres.  I need to think more in terms of staples.  I don't want to create a novelty game that is more original than practical.

-- 8/15/2019 -- I'm realizing I can't just throw out simulation like that.  One of the fundamental tenants of this game is to create a world that does not revolve around the character.  That implies simulation.  Also, one of my primary inspirations are dreams that feel very much like simulation.  Secondly, I love simulation and always gravitate toward it.  Thirdly, while the gameplay part of this game keeps staggering, the more general world building is having an easier time keeping my interest and moving forward.

With that said, I need to better address the reasons why I felt like I needed to move away from simulation.

The first concern is none of my favorite games are simulations.  The simulation games I've liked the most end up feeling like novelties to me. (Though technically I suppose all games should be categorized as novelties.)  The closest exception to this would be classic AOS maps, which would simulate a battle between two forces without any player interaction, and I loved those games, (though I also reached the conclusion that there's only so far you can take that genre and have never seriously pursued making my own version.)

The second concern, which probably ties into the first concern, is simulations do not naturally lend themselves to gameplay.  They put the focus on making an interesting world, and then designers make a game on top of that.  It's not a straight line to fun gameplay.  Simulations allow for rich emergent gameplay, but I'm increasingly finding emergent anything to be overrated.  (By the time emergence is tamed and contained enough so that it does more good than harm, most of the initial cost saving and wow-factor that initially attract people to emergence is spent.)

I probably need a dedicated section discussing emergence.  The main point could probably be summed up as, "emergence is impractical until all of its possible outputs are at least generally accounted for."  It can be fun when an author is surprised by his own creation, but most of the time such surprises are flaws, not features.  This point is a little depressing, and a little disenchanting, but I think it is also healthy.  Authors tend to look at their creation as an idol—look to it for life it cannot have and fulfillment it cannot give.

If any part of your system is not bolted down, it will cause more harm than good.

During the creative process, less than 1% of all accidents are happy.  The problem is that any artist worth anything fixes the bad accidents as they come, and only leaves the happy accidents.  So the bad accidents live short lives, while the happy accidents become immortalized.  Thus, accidents tend to take on a better reputation than they deserve.

If an author is looking for awe, they should spend more time looking for it outside of their own creations.

Authors long to be wowed by their own creations.

### UI

For at least the past decade somewhere somehow I got this idea that games need to be as single view as possible and maximize that single view.  I think my main concerns have been:

* Immersion - The most immersive games only use one view for gameplay
  * Half-Life is famous for not having loading screens.
* Focus - I'm impressed by games that can stretch a single UI very far and do everything with it.
  * Example: Quake removing the need for a key press to interact with the environment, opting instead for doors that automatically open when you are nearby and wall buttons that can be pushed or shot.
* Minigames - I don't like minigames and they tend to be the extreme use of different UIs

I'm done with those concerns.  I want this game to be immersive where its reasonable but am no longer interested in forcing the issue.  A one-size-fits all UI is a very limiting.  Use the right tool for each job.

The main reason I don't like minigames is because they minimize relationship across components, and I love it when things are deeply relational.  Each minigame has little influence on its siblings. At one point I was throwing around ideas for a game that had deeply relational minigames but that is challenging and not a natural, obvious minigame structure.

Part of what defines a minigame is not simply the change in UI but the change in data.  The minigame has its own set of contained data it is working with.

Breaking the game into different UIs is less of a problem if they are sharing the same data.  An inventory screen shows the player information that is currently a part of the larger game state yet hard to see in other game views.

### Narrative

Narrative vs. gameplay is the elephant in the room that I have been both fighting against and avoiding dealing with.  They are oil and water and for years I have striven to merge them together in a way I have yet to see any other game do.

I need to stop trying to do that.  I need to work within the limitations I've seen in other games and slowly push the boundaries by using a mix of the best-of ingredients other existing games use.

When push comes to shove, when I make a game, I want it to be gameplay first, narrative second.  I love it when a game has a *sense* of story.  DOOM and Dark Souls are good examples of this.

### Base Micro-game

While I love character development, that is still a macro layer.  Games need a simple, core micro layer.  The activities and challenges that are the foundation of the gameplay.  This is especially important because it acts as the game design "fallback".  The game designer can add all sorts of additional layers and subsystems but any point those additions aren't being used, this is the layer the game defaults to.

This defines the bread and butter of a game.  Some of the most common staples are:

1. Kill things (Diablo II)
2. Gather resources (Don't Starve)
3. Hide from enemies (Thief)
4. Tactics - (Tales of Maj'Eyal)
5. Strategy - (Dark Souls)

Out of those options, strategy is the most appealing to me.  Tactics is the second most appealing activity, though it is harder to implement deeply in a 3D action game.  I'd like to implement tactics where possible but it won't be the core of the gameplay.

That's a key clarification.  There can only be one default.  A good game can feature all of the above activities but only one can be the fallback.  Otherwise the game loses focus and will invariably become a mess.  Game activities need to be prioritized.

Both Tales of Maj'Eyal and Dark Souls have tactics and strategy but the difference is:

* In TOME most of the decisions of execution are made during an encounter
* In Dark Souls most of the decisions of execution are made prior to the encounter

The one exception to TOME is on the harder difficulties players sometimes use completely different equipment for different encounters.  This isn't needed for the normal or nightmare difficulties.  It's also noted that at that point while different loadouts are important, the game wasn't really designed for that and doing so is a little bit of a tedious hack.
