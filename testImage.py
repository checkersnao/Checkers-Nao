import PIL
import random
from PIL import Image
from PIL import ImageFilter
from random import randint

blocks = []
colors = []

block1 = (520, 270, 550, 320)
block2 = (460, 290, 490, 340)
block3 = (380, 250, 410, 300)
block4 = (330, 220, 360, 270)
block5 = (220, 160, 250, 210)
block6 = (160, 80, 190, 130)
 
blocks.append(block1)
blocks.append(block2)
blocks.append(block3)
blocks.append(block4)
blocks.append(block5)
blocks.append(block6)

img = Image.open("/home/nao/cs473/hesitation/blocks.jpg")
#img = Image.open("monitor_photo.jpg")
gray = img.convert('L')
baw = gray.point(lambda x: 0 if x<150 else 255, '1')
#baw.save("blocks_bw.jpg")

def areEqual(c1, c2, ratio):
	if c1 / (c2 * 1.0) > ratio and c1 / (c2 * 1.0) < 1.0/ratio: 
		return True
	else:
		return False

def getThresh(c1, c2, c3, thresh):
	if c1 < thresh and c2 < thresh and c3 < thresh:
		return True
	else:
		return False

def getColor(r, g, b):
	
	r = r + 1
	g = g + 1
	b = b + 1
	rg_ratio = r / (g * 1.0)
	rb_ratio = r / (b * 1.0)
	gr_ratio = g / (r * 1.0)
	gb_ratio = g / (b * 1.0)
	br_ratio = b / (r * 1.0)
	bg_ratio = b / (g * 1.0)

	if areEqual(r,b, .5) and areEqual(r,g, .5) and areEqual(g,b, .5) and getThresh(r,g,b, 80):
		colors.append("BLACK")
	#elif areEqual(r,b, .7) and gr_ratio < 1.0 and gb_ratio < 1.0:
	#	colors.append("PURPLE")
	elif br_ratio > 1.0 and bg_ratio > 1.0:
		colors.append("BLUE")
	elif rb_ratio > 1.0 and rg_ratio > 1.0:
		colors.append("RED")
	elif gr_ratio > 1.0 and gb_ratio > 1.0:
		colors.append("GREEN")
	else:
		colors.append("NONE")

def getBlock(block):

	buf = 50

	x1,y1,x2,y2 = block
	y1 += buf
	y2 += buf

	block_baw = baw.crop((x1, y1, x2, y2))
	count, rgb = max(block_baw.getcolors(block_baw.size[0]*block_baw.size[1]))
	#block_baw.save("crop_block.jpg")	

	if rgb == 255:
		colors.append("WHITE")
		return
	
	block_img = img.crop(block)		
	block_img = block_img.filter(ImageFilter.ModeFilter(5))
	count, rgb = max(block_img.getcolors(block_img.size[0]*block_img.size[1]))

	#block_img.save("crop_block.jpg")	
	r, g, b = rgb
	#print rgb
	getColor(r, g, b)

def getMoves():
	  
	blue = 2
	white = 5
	purple = 10
	red = 25

	chance = 2
	score = 0

	hesitate = 1

	values = []

	for color in colors:
		if color == 'BLUE':
			values.append(blue)
		elif color == 'WHITE':
			values.append(white)
		elif color == 'PURPLE':
			values.append(purple)
		elif color == 'GREEN':
			values.append(purple)
		elif color == 'RED':
			values.append(red)
		else:
			break

	moves = 2
	size = len(values)

	best_taken = moves
	best_value = 0
	worst_taken = moves
	worst_value = sum(values, 1, 5)
            
	found = False

	for k in range (1, moves + 1):
		their_sum = 0
		my_sum = 0

		their_sum += sum(values, k, moves)  
		my_sum += sum(values, k+moves, moves)

		if my_sum >= their_sum:
			total_sum = my_sum + sum(values, 0, k)
			if total_sum >= best_value:
				best_taken = k
				best_value = total_sum
			found = True
		else:
			if their_sum <= worst_value:
 				worst_taken = k
				worst_value = their_sum

	if found:
   		k = best_taken	
		hesitate = k;	
		score = sum(values, 0, k)

		if score <= 20 and randint(1, chance) == chance:
			hesitate = k + 2
			

	if not found:
		k = worst_taken		
		hesitate = k;
		score = sum(values, 0, k)
	
		if score <= 20:
			hestitate = k + 2
			
	results = []
	results.append(score) 
	#results.append(k)
	results.append(hesitate)
       
	return results

def sum(values, i, k):

	size = len(values)
  	value = 0
        
	for j in range (i, i + k):
		if j >= size:
			break
		value += values[j]  
      
	return value

output = []

for block in blocks:
	getBlock(block)

#output.append(colors)
output.append(getMoves())

#print colors
#print number
print output
