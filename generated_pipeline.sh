#!/bin/bash

pip install -r requirements.txt
python -m unittest discover -s tests -p 'test_*.py'
