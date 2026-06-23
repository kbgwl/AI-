#!/bin/bash
cd /mnt/d/AI客服系统/客服系统-前端
nohup npx vite > /mnt/d/AI客服系统/客服系统-前端/frontend.log 2>&1 &
echo $! > /mnt/d/AI客服系统/前端PID.txt
