#!/bin/bash

log_file="mylog.log"
max_log_size=$((1024*1024*1024)) # 1GB in bytes

while true; do
   if [ -e "$log_file" ]; then
       current_size=$(stat -c %s "$log_file" 2>/dev/null)

       if [ -n "$current_size" ] && [ $current_size -ge $max_log_size ]; then
           mv "$log_file" "$log_file.old"
           touch "$log_file"
       fi
   fi
   
   sleep 10 # Adjust the sleep interval as needed
done
