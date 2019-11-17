## Guidelines for World Generation

1. Avoid code that steps backward or fixes previous steps.  Such steps have a chance of breaking previous steps.  Each step in the pipeline should output data that can seamlessly be consumed by later steps.
   1. This will sometimes require adding new intermediate steps and data structures.
2. Avoid taking turns alternating between different operations.  Pipelines should be purely linear in transitioning from one type of operation to the next.  Mixing operations in the same step can lead to them stepping on each other's feet—basically a form of race condition.  Worst case, this can lead to infinite loops.
3. Avoid writing code that handles fringe cases in its input.  If a step in the pipeline is breaking due to fringe cases, change upstream algorithms to prevent those fringe cases.
   1. Sometimes this is simply a matter of choosing the right upstream algorithm.
   2. Sometimes this will require additional rules applied to upstream steps.
4. Steps do not need to be general purpose.  It is fine for a step in the pipeline to return output specialized to make later steps easier.
5. The pipeline should seamlessly and gradually transition from proactive steps to reactive steps.  
6. It is easier to specify logic in earlier steps than later steps.  Each step of the pipeline adds additional commitments and restrictions to the data.
7. Use commutative algorithms wherever possible.  Noncommutative algorithms increase the potential for side-effects and fringe cases.
8. Avoid using side-effects as a source of randomness.  Any randomness should be carefully injected into the pipeline and controlled.