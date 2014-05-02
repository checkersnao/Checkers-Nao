import PIL
import random
from PIL import Image
from PIL import ImageFilter
from random import randint

blocks = []
colors = []

#rectangle of pixel locations for each block
block1 = (510, 330, 530, 380)
block2 = (430, 350, 450, 400)
block3 = (370, 320, 390, 370)
block4 = (300, 310, 320, 360)
block5 = (220, 260, 240, 310)
block6 = (140, 200, 160, 250)
 
blocks.append(block1)
blocks.append(block2)
blocks.append(block3)
blocks.append(block4)
blocks.append(block5)
blocks.append(block6)

#open the image from the nao
img = Image.open("/home/nao/cs473/hesitation/blocks.jpg")
gray = img.convert('L')
baw = gray.point(lambda x: 0 if x<150 else 255, '1')

#test if r, g, or b values fall with a set ratio
def areEqual(c1, c2, ratio):
	if c1 / (c2 * 1.0) > ratio and c1 / (c2 * 1.0) < 1.0/ratio: 
		return True
	else:
		return False

#test if rgb values are below a threshold
def getThresh(c1, c2, c3, thresh):
	if c1 < thresh and c2 < thresh and c3 < thresh:
		return True
	else:
		return False

#determine block color given r, g, and b values
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

	#check if r, g and values are similar and less than 80
	if areEqual(r,b, .5) and areEqual(r,g, .5) and areEqual(g,b, .5) and getThresh(r,g,b, 80):
		colors.append("BLACK")
	#check if b value is greater
	elif br_ratio >= 1.0 and bg_ratio >= 1.0:
		colors.append("BLUE")
	#check if r value is greater
	elif rb_ratio >= 1.0 and rg_ratio >= 1.0:
		colors.append("RED")
	#check if g value is greater
	elif gr_ratio >= 1.0 and gb_ratio >= 1.0:
		colors.append("GREEN")
	else:
		colors.append("NONE")

#get the block of each block
def getBlock(block):

	buf = 15

	x1,y1,x2,y2 = block
	y1 += buf
	y2 += buf

	#look at side of block and get rgb values
	block_baw = baw.crop((x1, y1, x2, y2))
	count, rgb = max(block_baw.getcolors(block_baw.size[0]*block_baw.size[1]))
	if rgb == 255:
		colors.append("WHITE")
		return
	
	#look at top of block and get rgb values
	block_img = img.crop(block)		
	block_img = block_img.filter(ImageFilter.ModeFilter(5))
	count, rgb = max(block_img.getcolors(block_img.size[0]*block_img.size[1]))

	r, g, b = rgb
	getColor(r, g, b)

#run the algorithm to determine number of moves
def getMoves():

	#block point values
	blue = 2
	white = 5
	purple = 10
	red = 25

	#chance of hesitation
	chance = 2

	#total score
	score = 0

	#value to return
	hesitate = 1

	values = []

	for color in colors:
		if color == 'BLUE':
			values.append(blue)
		elif color == 'WHITE':
			values.append(white)
		elif color == 'GREEN':
			values.append(purple)
		elif color == 'RED':
			values.append(red)
		else:
			break

	gameOver = False

	moves = 2
	size = len(values)

	if size <= 2:
		gameOver = True

	#initialize values
	best_taken = moves
	best_value = 0
	worst_value = sum(values, 1, 5)
	worst_taken = 0        

	found = False

	#loop at all four states
	for k in range (1, moves + 1):

		if k > size:
			break

		their_sum = 0
		my_sum = 0

		#find our best value and their best value
		their_sum += sum(values, k, moves)  
		my_sum += sum(values, k+moves, moves)

		#check if our sum is greater, update value
		if my_sum >= their_sum:
			total_sum = my_sum + sum(values, 0, k)
			if total_sum >= best_value:
				best_taken = k
				best_value = total_sum
			found = True
		#check if their sum is greater, update value
		else:
			if their_sum <= worst_value:
 				worst_taken = k
				worst_value = their_sum

	#if we can do better
	if found:
   		k = best_taken	
		hesitate = k;	
		score = sum(values, 0, k)

		#randomly choose to hesitate
		if score <= 20 and randint(1, chance) == chance and not gameOver:
			hesitate = k + 2			

	#if we can't do better
	if not found:
		k = worst_taken		
		hesitate = k;
		score = sum(values, 0, k)
	
		#random choose to hesitate
		if score <= 20 and not gameOver:
			hestitate = k + 2

	if gameOver:
		hesitate += 5
			
	results = []
	results.append(score) 
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

#get block colors and find moves
for block in blocks:
	getBlock(block)

output.append(getMoves())

print output