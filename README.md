# Gaussian
It's single page web app with elements which allows you to upload picture, configure blur radius, process it and download result.

In this project i implement Gaussian blur algorithm with multithreading. Picture is considered as 2D array of pixels, so we can split it to many smaller chunks, which can be processed independently.

For web app I used servlets.
For multi-threading ExecutorService and Runable were used.
         ( There was no need to use Callable and Future, because it works directly with picture bits. It helps me to reduse memory using.)
For project building I used Maven.
