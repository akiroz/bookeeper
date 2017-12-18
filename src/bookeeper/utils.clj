(ns bookeeper.utils)

(defmacro def-components
  [lib & components]
  `(do
     ~@(for [sym components]
         `(def ~sym (~'adapt-react-class (~'oget ~lib ~(str sym)))))))

(defmacro def-apis
  [lib & apis]
  `(do
     ~@(for [sym apis]
         `(def ~sym (~'oget ~lib ~(str sym))))))

