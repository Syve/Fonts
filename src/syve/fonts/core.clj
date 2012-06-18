(ns syve.fonts.core)

(defprotocol IFont
  (width [this str] "Get the rendered width of the given string.")
  (height [this str] "Get the rendered height of the given string.")
  (line-height [this] "Get the max height of any line drawn by this font.")
  (render ^BufferedImage [this s] "Render the given string into a BufferedImage"))

