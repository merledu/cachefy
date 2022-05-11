Cachefy
=======================
![Apache License](https://img.shields.io/github/license/merledu/cachefy?style=plastic)
![GitHub contributors](https://img.shields.io/github/contributors/merledu/cachefy?style=plastic)

A CHISEL Framework that provides Plug n Play API for connecting Caches in any of your CHISEL designs.

This Framework works in sync with [Jigsaw](https://github.com/merledu/jigsaw) Framework which provides memory devices in CHISEL designs, with which caches can be connected on.

This is not a standalone repository. It is a part of [SoC-Now-Generator](https://github.com/merledu/SoC-Now-Generator) .

### Architecture

<img src="https://github.com/shahzaibk23/CHISEL-Caches/blob/master/docs/arch.png?raw=true" />


### Current Cache Implementations

| Module  | Purpose |
| ------------- | ------------- |
| DMCacheWrapper | Direct Mapped Cache that will communicate with Jigsaw Data Mem automatically on misses |
| DMCache | Tilelink Cached Compatible Direct Mapped Cache which will respond only Hits and Misses, TL-C shall do the rest "
### Dependencies

#### JDK 8 or newer

We recommend LTS releases Java 8 and Java 11. You can install the JDK as recommended by your operating system, or use the prebuilt binaries from [AdoptOpenJDK](https://adoptopenjdk.net/).

#### SBT 

SBT is the most common built tool in the Scala community. You can download it [here](https://www.scala-sbt.org/download.html).  


### How to get started

Fork this repository on your own individual profiles. After forking clone the repository and run:

```sh
sbt test
```

You should see a whole bunch of output that ends with something like the following lines
```
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 5 s, completed Dec 16, 2020 12:18:44 PM
```
If you see the above then...

### It worked!

### For quick debugging
If you quickly want to see what verilog is being generated, go to this link  https://bit.ly/3u3zr0e and write Chisel here.
