# Contributing

## Ordering class methods

We follow the following ordering of class methods.

1. Constructors first.
2. Static methods next;
    1. if there is a main method, always before other static methods.
3. Non-static methods next, overriden methods first, in order of the significance of the method followed by any methods that it calls: this
   means that public methods that call other class methods appear towards the top and private methods that call no other methods usually end
   up towards the bottom.
4. Standard methods like `toString`, `equals` and `hashcode` next.
5. Getters and setters have a special place reserved right at the bottom of the class.

Inner classes are placed at the bottom of the containing class.