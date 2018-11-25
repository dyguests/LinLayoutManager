# Custom Tutorial(A Instance)(Hands on)

## Step1. Design your curve.

your can design your curve anyway.

![](./graphics/tutorial/tutorial_1.png)

[sources link.](https://www.desmos.com/calculator/ffeu06bgul)

## Step2. Parse to sources.

![](./graphics/tutorial/tutorial_2.png)

run App:

![](./graphics/tutorial/tutorial_3.png)

## Step3. Parse the Coordinate System.

From right-up coordinate System to right-down coordinate System.

`y = 1 - points.asSequence().mapIndexed { index, vector2 -> b(t, index + 1, vector2.y) }.sum()`

## Step3. Set init offset.

The first item should be center of screen, it means t=0.5 not t=0.