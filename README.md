# Spice of Life: Stick Edition

<p align="center">
  <img width="300" height="300" alt="logo_480_v2" src="https://github.com/user-attachments/assets/64bda648-904b-4504-8c8d-52cfc3e80763" />
</p>

<p align="center"><em>Motivating people to explore new foods with the stick, not the carrot.</em></p>

## Features

- A simple but effective food system that promotes food diversity by reducing a food's nutrition and saturation each time you eat it. The only way to raise its effectiveness back up is to simply eat different foods!

- Adds a handy dandy **Lunch Bag** item that allows you to store multiple stacks of food into a single inventory slot, working similarly to a bundle.

- `/solstick stomach` An easy way to view the contents of your tummy and see what foods have lost some of their effectiveness.

- AppleSkin compatibility allows you to easily view the current nutrition and saturation of food vs. its max effectiveness.
  - <em>It is highly recommended that you have AppleSkin installed with this mod!</em>

- Fully multiplayer supported, each player has a unique stomach.

- Compatible with other Spice of Life mods!

<p align="center">
    <img width="435" height="218" alt="too_much_steak" src="https://github.com/user-attachments/assets/eec5b256-55b4-4d92-979a-b7c4828ab2e3" />
</p>

<p align="center"><em>my poor ol' steak sitting at 50% of its true potential because it is the only thing i eat</em></p>

## Description

Do you miss the roots of the original spice of life? Looking for a version of Spice of Life that does not grant you excessive buffs? Do you just need a slight nudge to try out other foods? You found the mod.

This mod promotes food diversity in a slightly more challenging but still rewarding way. Instead of rewarding players for trying new foods, repeatedly eating the same food reduces its effectiveness. Maintaining a varied diet restores foods to their normal effectiveness.

This mod is intended to still be forigving and be just a nudge. You won't witness dramatic drops in effectiveness. You will most likely juggle about 6 or 7 different foods at a time. To make juggling foods easier, there is a new Lunch Bag item to store multiple stacks of food into 1 slot.

## Configuration

The config file is very simple to use and can be adjusted to your preferred playstyle. As stated above, the default configuration is tuned for you to juggle 6 or 7 different foods

- Stomach size - The stomach works like a queue. As you eat, a food loses its strength the more times it appears in your stomach. The stomach/queue works in a FIFO manner, meaning the first food you eat will be the first one to leave once the stomach/queue is full. Making this number larger means food will sit longer with less effectiveness and will increase difficulty. Making this number smaller means food will exit sooner, returning to its normal effectiveness quicker, making it much easier to manage.

- Decay rate percent - Decides how much effectiveness a food will lose for each time it appears in the stomach. Works as a multiplicative decay. For example, assuming a 6% decay rate, my Steak is now about 88% effective because it appears twice in my stomach (100% -> 94% -> 88.36%). This means the more you eat the same food, it will slowly plateau in how much effectiveness it loses.

