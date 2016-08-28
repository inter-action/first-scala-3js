# see http://www.ruanyifeng.com/blog/2015/03/build-website-with-make.html
# for more info on Makefile
PATH  := node_modules/.bin:$(PATH)
SHELL := /bin/bash

#variables
TARGET_DIR := target/scala-2.11/classes/webapp

.PHONY: 
# start of tasks/rules

serve:
	cd $(TARGET_DIR)
	python3 -m http.server
