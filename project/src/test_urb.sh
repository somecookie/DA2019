init_time=2
time_to_finish=10
make clean
make

cd src
echo "3
1 127.0.0.1 12341
2 127.0.0.1 12342
3 127.0.0.1 12343" > membership

for i in `seq 1 3`
do
    java ch.epfl.da.Main $i membership 5 & da_proc_id[$i]=$!
done

sleep $init_time

for i in `seq 1 3`
do
	kill -USR2 "${da_proc_id[$i]}"
done

sleep $time_to_finish

for i in `seq 1 3`
do
	kill -TERM "${da_proc_id[$i]}"
done
