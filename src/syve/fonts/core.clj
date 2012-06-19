(ns syve.fonts.core
  (:import [java.awt.image BufferedImage]
           [java.awt
            Font FontMetrics
            Graphics2D RenderingHints
            Color]))

(def render-cache-width 512)
(def render-cache-height 512)

(defprotocol IFont
  (width [this str] "Get the rendered width of the given string.")
  (height [this str] "Get the rendered height of the given string.")
  (line-height [this] "Get the max height of any line drawn by this font.")
  (render ^BufferedImage [this s] "Render the given string into a BufferedImage"))

(defn char-size
  "Given a font and a char return a map containing :width and :height properties."
  [font c & anti-alias?]
  (let [gfx (doto (-> (BufferedImage. 1 1 BufferedImage/TYPE_INT_ARGB) .createGraphics)
              (.setFont font))]
    (when anti-alias?
      (.setRenderingHint gfx RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))

    (let [metrics (.getFontMetrics gfx)]
      {:width  (max 1 (.charWidth metrics c))
       :height (if (<= (.getHeight metrics) 0) (.getSize font) (.getHeight metrics))})))

(defn char-image
  "Given a font and a char, return a BufferedImage depicting c in font."
  [font c & anti-alias?]
  (when c
    (let [{w :width, h :height} (char-size font c anti-alias?)
          img (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
          gfx (.createGraphics img)]
      (when anti-alias?
        (.setRenderingHint gfx RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))

      (doto gfx
        (.setFont font)
        (.setColor Color/WHITE)
        (.drawString (str c) 0 (-> gfx .getFontMetrics .getAscent)))

      img)))

(defmacro debug
  [form]
  `(let [res# ~form]
     (println '~form "=>" res#)
     res#))

(defn create-render-cache
  "Render all the characters into an image and return that image."
  [font chars & anti-alias?]
  (let [{width :width, height :height} (reduce (fn [{w1 :width, h1 :height}
                                                   {w2 :width, h2 :height}]
                                                 {:width (+ w1 w2)
                                                  :height (max h1 h2)})
                                               (map #(char-size font % anti-alias?) chars))
        img (BufferedImage. width height  BufferedImage/TYPE_INT_ARGB)
        gfx (.getGraphics img)]

    (doto gfx
      (.setColor (Color. 255 255 255 1))
      (.fillRect 0 0 width height))

    (loop [char (char-image font (first (seq chars)) anti-alias?)
           chars (rest (seq chars))
           x-pos 0]

      (when-not (nil? char)
        (.drawImage gfx char x-pos 0 nil)
        (recur (char-image font (first chars) anti-alias?)
               (rest chars)
               (+ x-pos (.getWidth char)))))

    img))