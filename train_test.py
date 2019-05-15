import tensorflow as tf
import numpy as np
import socket
import cv2
import os

def run_avg(image, aWeight):
	global bg
	# initialize the background
	if bg is None:
		bg = image.copy().astype("float")
		return

	# compute weighted average, accumulate it and update the background
	cv2.accumulateWeighted(image, bg, aWeight)

def segment(image, threshold=50):
	global bg
	# find the absolute difference between background and current frame
	diff = cv2.absdiff(bg.astype("uint8"), image)

	# threshold the diff image so that we get the foreground
	thresholded = cv2.threshold(diff,
								threshold,
								255,
								cv2.THRESH_BINARY)[1]

	# get the contours in the thresholded image
	(cnts, _) = cv2.findContours(thresholded.copy(),
									cv2.RETR_EXTERNAL,
									cv2.CHAIN_APPROX_SIMPLE)

	# return None, if no contours detected
	if len(cnts) == 0:
		return
	else:
		# based on contour area, get the maximum contour which is the hand
		segmented = max(cnts, key=cv2.contourArea)
		return (thresholded, segmented)


if __name__ == "__main__":
	
	# initialize weight for running average
	aWeight = 0.5

	# get the reference to the webcam
	camera = cv2.VideoCapture(0)

	# region of interest (ROI) coordinates
	top, left, bottom, right = 10, 630 - 300, 10 + 300, 630

	# initialize num of frames
	num_frames = 0
	
	# extracted background
	bg = None

	# the server's hostname or IP address
	HOST = '127.0.0.1'	
	
	# the port used by the server
	PORT = 8080			
	
	class_names = ['north', 'south', 'east', 'west']
	org_train_images = []
	train_images = []
	train_labels = []
	image_train_size = 28

	last_send_ch = ' '
	counter = 0

	LOAD_MODEL = True
	
	if LOAD_MODEL == False:
		for i in range(len(class_names)):
			for j in os.listdir('images/' + class_names[i]):
				img = cv2.imread('images/' + class_names[i] + '/' + j , 0)
				org_train_images.append(img)
					
				img = cv2.resize(img, (image_train_size, image_train_size))
				img = np.transpose(img)
				img = img.reshape((image_train_size, image_train_size, 1))
				
				train_images.append(img)
				train_labels.append(i)

		train_images = np.array(train_images) / 255.0
		train_labels = np.array(train_labels)

		model = tf.keras.models.Sequential([
		  tf.keras.layers.Conv2D(32, kernel_size=(3, 3), activation='relu', input_shape=(image_train_size, image_train_size, 1)),
		  tf.keras.layers.Conv2D(64, (3, 3), activation='relu'),
		  tf.keras.layers.MaxPooling2D(pool_size=(2, 2)),
		  tf.keras.layers.Dropout(0.25),
		  tf.keras.layers.Flatten(),
		  tf.keras.layers.Dense(128, activation='relu'),
		  tf.keras.layers.Dropout(0.5),
		  tf.keras.layers.Dense(128, activation='relu'),
		  tf.keras.layers.Dropout(0.25),
		  tf.keras.layers.Dense(4, activation='softmax')
		])

		model.compile(optimizer='adam',
					  loss='sparse_categorical_crossentropy',
					  metrics=['accuracy'])

		model.fit(train_images, train_labels, epochs=8)

		# Save the model
		model.save('models/model.h5')
	else:
		# Recreate the exact same model purely from the file
		model = tf.keras.models.load_model('models/model.h5')


	# start connection with game server
	with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
		s.connect((HOST, PORT))
	
		# keep looping, until interrupted
		while(True):
			# get the current frame
			(grabbed, frame) = camera.read()

			# flip the frame so that it is not the mirror view
			frame = cv2.flip(frame, 1)

			# clone the frame
			clone = frame.copy()

			# get the height and width of the frame
			(height, width) = frame.shape[:2]

			# get the ROI
			roi = frame[top:bottom, left:right]

			# convert the roi to grayscale and blur it
			gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
			gray = cv2.GaussianBlur(gray, (7, 7), 0)

			# to get the background, keep looking till a threshold is reached
			# so that our running average model gets calibrated
			if num_frames < 30:
				run_avg(gray, aWeight)
			else:
				# segment the hand region
				hand = segment(gray)

				# check whether hand region is segmented
				if hand is not None:
					# if yes, unpack the thresholded image and
					# segmented region
					(thresholded, segmented) = hand
					
					# draw the segmented region and display the frame
					cv2.drawContours(clone, [segmented + (left, top)], -1, (0, 0, 255))
					cv2.imshow("Thesholded", thresholded)
					
					# test model
					thresholded = cv2.resize(thresholded, (image_train_size, image_train_size))
					thresholded = np.transpose(thresholded)
					thresholded = thresholded.reshape((image_train_size, image_train_size, 1))
					test_image = np.array([thresholded]) / 255.0
					
					res = model.predict(test_image)
					
					
					if last_send_ch == ' ' or last_send_ch == class_names[np.argmax(res)][0:1]:
						counter += 1
					else:
						counter = 0
					
					last_send_ch = class_names[np.argmax(res)][0:1]
					
					if counter == 2:
						counter = 0
						s.sendall(bytes(last_send_ch + '\n', "utf-8"))

			# draw the segmented hand
			cv2.rectangle(clone, (left, top), (right, bottom), (0,255,0), 2)

			# increment the number of frames
			num_frames += 1

			# display the frame with segmented hand
			cv2.imshow("Video Feed", clone)

			# observe the keypress by the user
			keypress = cv2.waitKey(1) & 0xFF

			# if the user pressed "q", then stop looping
			if keypress == ord("q"):
				break

# free up memory
camera.release()
cv2.destroyAllWindows()


