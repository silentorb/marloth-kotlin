
### Main workflow

[*] New turn queue is prepared.  Player is at the start of the queue.

[*] No significant state changes occur until player uses an ability.

[*] Client sends a command to the server.

[*] Server listens for commands.  

[*] When the server receieves a command it performs the first half of the logic, and then creates an newAnimation.

[*] The server updates the newAnimation.

[ ] While the newAnimation is active, the client also renders the newAnimation.

[ ] When the newAnimation ends, the server performs the second half of the logic.

[ ] This can be repeated several times.

[ ] Once the newAnimation/logic chain is complete, the next server handles the first half of the logic and creates an newAnimation.

[ ] The same logic/newAnimation workflow is repeated.

[ ] Once the turn queue is empty, the server creates a new round with a new turn queue.
 