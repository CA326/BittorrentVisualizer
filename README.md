##CA326 Third year project.

Currently: Implementing BitTorrent protocol.
After we have a stable implementation of the protocol we are going to incorporate events to tie the implementation in with our visualisation.

###Progress:
- Can parse metainfo file.
- Successfully contacts tracker and parses response
- Connects to peers.
- Handshakes with peers.
- Requests pieces from peers.
- Parses peer messages.
- Writes downloaded pieces to disk.

###TODO:
- Need to deal with multi-file torrents, first by checking the metainfo file.
- Make class Tracker a thread so we can send stats and receive updates at a regular interval.
- Model Messages as classes.
- Make class Peer a thread.
- Come up with and implement a file caching algorithm.
- Need to group classes in packages.
- Refinement in all classes, a lot of the current code is very untidy and contains some bad practices (imports, exceptions).
