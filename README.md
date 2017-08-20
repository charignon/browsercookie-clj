# Command line tool to decrypt Chrome cookies

## Usage

    $ git clone https://github.com/charignon/browsercookie-clj.git
    $ cd browsercookie-clj
    $ brew install leiningen jq
    $ lein run "website.com" | jq .

## More information

Check out [https://blog.laurentcharignon.com/post/extracting-chrome-cookie-clojure/](https://blog.laurentcharignon.com/post/extracting-chrome-cookie-clojure/) for more information.


## License

Copyright © 2017 Laurent Charignon

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
