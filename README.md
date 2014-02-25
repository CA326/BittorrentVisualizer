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
- Need to deal with all possibilities in the meta-info file e.g multiple file torrents.
- Make class Tracker a thread so we can send stats and receive updates at a regular interval.
- Deal with UDP Trackers
- ~~Model Messages as classes.~~
- ~~Make class Peer a thread.~~
- Come up with and implement a file caching algorithm.
- ~~Implement a piece choosing algorithm.~~ (Need to improve)
- Refinement in all classes, a lot of the current code is very untidy and contains some bad practices (exceptions, preamble missing).
- ~~Group classes in packages.~~

###More TODO(Further away):
- Event handling.
- GUI

### Issues
#### 25th February 2014
- BDecoding fails on some tracker responses.
- Download sticks on last piece
