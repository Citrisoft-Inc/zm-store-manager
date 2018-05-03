class:
	buildr

clean:
	buildr clean

jar:
	buildr package

doc:
	buildr doc

all: jar doc
