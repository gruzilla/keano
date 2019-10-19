#!/bin/bash

rm keanomap.db; ./create_db.py; python3 tweepy_streamer.py -i