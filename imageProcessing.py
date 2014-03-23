#!/usr/bin/env python

import cv2
import numpy as np

#Color range thresholds for HSV
BLUE_MIN = np.array([110, 50, 50],np.uint8)
BLUE_MAX = np.array([130, 255, 255],np.uint8)
GREEN_MIN = np.array([50, 50, 50],np.uint8)
GREEN_MAX = np.array([70, 255, 255],np.uint8)
RED_MIN = np.array([0, 50, 50],np.uint8)
RED_MAX = np.array([10, 255, 255],np.uint8)


#Globals
blocks = []	# Holds the colors of the 6 blocks that the robot can see

def createThresholdImages(imgPath):
	# Read in the image
	img = cv2.imread(imgPath)

	# Array to hold the HSV threshold images created below for each
	# block color 
	threshImgs = []
	BLUE = cv2.cvtColor(img,cv2.COLOR_BGR2HSV)
	threshImgs.append(cv2.inRange(BLUE, BLUE_MIN, BLUE_MAX))
	
	GREEN = cv2.cvtColor(img,cv2.COLOR_BGR2HSV)
	threshImgs.append(cv2.inRange(GREEN, GREEN_MIN, GREEN_MAX))
		
	RED = cv2.cvtColor(img,cv2.COLOR_BGR2HSV)
	threshImgs.append(cv2.inRange(RED, RED_MIN, RED_MAX))
	
	WHITE = cv2.imread(imgPath, cv2.CV_LOAD_IMAGE_GRAYSCALE)
	(thresh, im_bw) = cv2.threshold(WHITE, 128, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
	threshImgs.append(im_bw)
	
	return threshImgs


def getBlockColor(startRow, endRow, startCol, endCol, threshImgs):
	# colors holds the count of white pixels which corresponds to if the block met
	# the threshold for that color
	colors = [['Blue', 0], ['Green', 0], ['Red', 0], ['White', 0]]
	
	# Go through the 10x10 patch of the block and increment the count of white
	# pixels for the specific color if the pixel is white
	for row in range(startRow, endRow + 1):
		for col in range(startCol, endCol+1):
			imgIndex = 0
			for threshImg in threshImgs:
				if threshImg[row, col] == 255:
					colors[imgIndex][1] = colors[imgIndex][1] + 1
				imgIndex = imgIndex + 1
	
	# find the blocks color by finding the color with most white pixels
	# this is just the simple alg of going through and saving the max
	blockColor = ""
	blockColorTally = 0
	for color in colors:
		if color[1] > blockColorTally:
			blockColor = color[0]
			blockColorTally = color[1]
	
	return blockColor


def getAllBlockColors(threshImgs):
	# Just gets the block colors for the 6 blocks, starting from the next to
	# block to be chosen
	blocks.append(getBlockColor(145, 155, 270, 280, threshImgs))# Block 1
	blocks.append(getBlockColor(145, 155, 220, 230, threshImgs))# Block 2
	blocks.append(getBlockColor(130, 140, 170, 180, threshImgs))# Block 3
	blocks.append(getBlockColor(95, 105, 130, 140, threshImgs))	# Block 4
	blocks.append(getBlockColor(65, 75, 80, 90, threshImgs))	# Block 5
	blocks.append(getBlockColor(20, 30, 5, 15, threshImgs))		# Block 6

if __name__ == "__main__":
	# Remember getBlockColor(startY, EndY, startX, EndX)
	threshImgs = createThresholdImages("Blocks.png")
	getAllBlockColors(threshImgs)
	print blocks