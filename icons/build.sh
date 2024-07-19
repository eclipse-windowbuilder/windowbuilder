#!/bin/bash

# Convert all svg to png files
SVG_FILES=$(find . -type f -name "*.svg")
for f in $SVG_FILES; do
  echo $f
  inkscape $f -d 96 -o ${f%.svg}.png;
  inkscape $f -d 192 -o ${f%.svg}@2x.png;
done 

# Move all png files to their respective bundles
rsync -rv --include='*.png' --include='*/' --exclude='*' --remove-source-files --prune-empty-dirs . ../
