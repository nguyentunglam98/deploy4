package com.example.webDemo3.service.impl.assignRedStarServiceImpl;

import com.example.webDemo3.constant.Constant;
import com.example.webDemo3.dto.MessageDTO;
import com.example.webDemo3.entity.Class;
import com.example.webDemo3.entity.ClassRedStar;
import com.example.webDemo3.entity.ClassRedStarId;
import com.example.webDemo3.entity.User;
import com.example.webDemo3.exception.MyException;
import com.example.webDemo3.repository.ClassRedStarRepository;
import com.example.webDemo3.repository.ClassRepository;
import com.example.webDemo3.repository.UserRepository;
import com.example.webDemo3.service.assignRedStarService.CreateAssignRedStarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class CreateAssignRedStarServiceImpl implements CreateAssignRedStarService {

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClassRedStarRepository classRedStarRepository;

    private int[] indexClassOfRedStar;
    private List<Integer>[] indexRedStarOfClass;
    private int[][] flag;
    private Random ran = new Random();
    private static int n = 1000;
    int[][] population;
    int[][] populationFlag;
    int[] costValue;
    int size;
    int[] outputData;
    int costValueOutput;

    @Override
    public MessageDTO delete(Date fromDate) {
        MessageDTO message = new MessageDTO();
        Date dateCurrent = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(sdf.format(dateCurrent).equalsIgnoreCase(sdf.format(fromDate))
                || fromDate.before(dateCurrent)){
            message = Constant.NOT_DELETE_ASSIGN_REDSTAR;
            return message;
        }
        try {
            classRedStarRepository.deleteByFromDate(fromDate);
        } catch (Exception e) {
            System.out.println(e);
            message.setMessageCode(1);
            message.setMessage(e.toString());
        }
        message = Constant.SUCCESS;
        return message;
    }

    @Override
    public MessageDTO checkDate(Date fromDate) {
        MessageDTO message = new MessageDTO();
        message.setMessageCode(0);
        Date dateCurrent = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(sdf.format(dateCurrent).equalsIgnoreCase(sdf.format(fromDate))
                || fromDate.before(dateCurrent)){
            message = Constant.NOT_ADD_ASSIGN_REDSTAR;
            return message;
        }
        List<Date> date = classRedStarRepository.findByDate(fromDate);
        if (date != null && date.size() > 0) {
            sdf = new SimpleDateFormat("dd/MM/yyyy");
            message.setMessageCode(2);
            message.setMessage("Phân công sau ngày " + sdf.format(fromDate) + " sẽ bị xóa.\n Bạn có muốn tiếp tục ghi đè?");
        }
        return message;
    }

    @Transactional
    @Override
    public MessageDTO create(Date fromDate) {
        MessageDTO message = Constant.SUCCESS;
        Date dateCurrent = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(sdf.format(dateCurrent).equalsIgnoreCase(sdf.format(fromDate))
                || fromDate.before(dateCurrent)){
            message = Constant.NOT_ADD_ASSIGN_REDSTAR;
            return message;
        }
        try {
            // delete if fromdate exit
            MessageDTO messageCheckDate = checkDate(fromDate);
            if (messageCheckDate.getMessageCode() == 2) {
                classRedStarRepository.deleteByFromDate(fromDate);
            }
            List<Class> classList = classRepository.findAll();
            List<User> redStarList = userRepository.findRedStar();
            Date beforDate = classRedStarRepository.getBiggestClosetDate(fromDate);
            List<ClassRedStar> assignList = new ArrayList<>();
            User[] assignUser = new User[0];
            if (beforDate != null) {
                assignList = classRedStarRepository.findAllByDate(beforDate);
                assignUser = new User[assignList.size()];
                for (int k = 0; k < assignList.size(); k++) {
                    ClassRedStar data = assignList.get(k);
                    User userData = userRepository.findUserByUsername(data.getClassRedStarId().getRED_STAR());
                    assignUser[k] = userData;
                }
            }
            getIndex(classList, redStarList, assignList, assignUser);

            //genertic
            //getFlag(classList, redStarList, assignList, assignUser);//tạo giá trị cho flag[][]
            khoitao(redStarList.size());
            for(int i = 1;i <= 1000; i++){
                danhgia(classList.size() * 2, redStarList.size());
                Print();
                if (costValueOutput == 0) {
                    System.out.println("tạo phân công thành công tại genertation thứ " + i);
                    break;
                }
                chonloc();
                dotbien();
            }
            if(costValueOutput > 0){
                System.out.println("giá trị nhỏ nhất là: " + costValueOutput);
            }

            insertClassRedStar(fromDate, classList, redStarList, outputData);

        } catch (Exception e) {
            System.out.println(e);
            message = new MessageDTO();
            message.setMessageCode(1);
            message.setMessage(e.toString());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return message;
    }

    private void insertClassRedStar(Date fromDate,
                                          List<Class> classList, List<User> redStarList, int[] output) throws Exception {
        int i;
        try {
            for (i = 0; i < size; i++) {
                int indexClass = i;
                if (indexClass != 0) indexClass = indexClass / 2;
                Class classi = classList.get(indexClass);
                User redStar = redStarList.get(output[i]);
                ClassRedStar data = new ClassRedStar();
                ClassRedStarId dataID = new ClassRedStarId();
                dataID.setRED_STAR(redStar.getUsername());
                dataID.setFROM_DATE(fromDate);
                data.setClassRedStarId(dataID);
                data.setClassSchool(new Class(classi.getClassId()));
                classRedStarRepository.save(data);
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new MyException(e.toString());
        }
    }

    private int[][] copyflag(int d,int c){
        int[][] flagcCopy = new int[d][c];
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < c; j++) {
                int value = flag[i][j];
                flagcCopy[i][j] = value;
            }
        }
        return flagcCopy;
    }

    private void getFlag(List<Class> classList, List<User> redStarList, List<ClassRedStar> assignList,User[] assignUser) {
        for (int i = 0; i < classList.size(); i++) {
            Class classi = classList.get(i);
            for (int j = 0; j < redStarList.size(); j++) {
                User redstar = redStarList.get(j);
                //loại bỏ cùng khối
                if (classi.getGrade() == redstar.getClassSchool().getGrade()) {
                    flag[i * 2][j] = -1;
                    flag[i * 2 + 1][j] = -1;
                }
                //loại bỏ chấm chéo 2 lần liên tiếp
                for (int k = 0; k < assignList.size(); k++) {
                    ClassRedStar data = assignList.get(k);
                    if ((data.getClassSchool().getClassId() == classi.getClassId())
                            && (data.getClassRedStarId().getRED_STAR() == redstar.getUsername())) {
                        flag[i * 2][j] = -1;
                        flag[i * 2 + 1][j] = -1;
                    }
                    User userData = assignUser[k];
                    if ((userData.getClassSchool().getClassId() == classi.getClassId())
                            && (data.getClassSchool().getClassId() == redstar.getClassSchool().getClassId())) {
                        flag[i * 2][j] = -1;
                        flag[i * 2 + 1][j] = -1;
                    }
                }
            }
        }
    }

    private void getIndex(List<Class> classList, List<User> redStarList,
                          List<ClassRedStar> assignList,User[] assignUser) {
        //set up
        size = classList.size() * 2;
        population = new int[n][size];
        populationFlag = new int[n][size];
        costValue = new int[n];
        indexClassOfRedStar = new int[redStarList.size()];
        indexRedStarOfClass = (List<Integer>[]) new List[classList.size()];
        flag = new int[classList.size() * 2][redStarList.size()];
        outputData = new int[size];
        costValueOutput = size;

        for (int i = 0; i < classList.size(); i++) {
            Class classi = classList.get(i);
            indexRedStarOfClass[i] = new ArrayList<>();
            for (int j = 0; j < redStarList.size(); j++) {
                User redstar = redStarList.get(j);
                if (classi.getClassId() == redstar.getClassSchool().getClassId()) {
                    indexRedStarOfClass[i].add(j);
                    indexClassOfRedStar[j] = i;
                }
                //fill all flag = 0
                flag[i * 2][j] = 0;
                flag[i * 2 + 1][j] = 0;
            }
        }

        getFlag(classList, redStarList, assignList, assignUser);//tạo giá trị cho flag[][]
        
    }

    private void khoitao(int redStarListSize) {
        int[] data = new int[redStarListSize];
        for (int i = 0; i < redStarListSize; i++) {
            data[i] = i;
        }
        for (int i = 0; i < n; i++) {
            int[] copyData = data.clone();
            int max = redStarListSize;
            for (int j = 0; j < size; j++) {
                int value = ran.nextInt(max);
                population[i][j] = copyData[value];
                max--;
                copyData[value] = copyData[max];
            }
        }
    }

    private void danhgia(int d,int c) {
        for (int i = 0; i < n; i++) {
            costValue[i] = 0;
            int[][] flagCopy = copyflag(d,c);
            for (int classIndex = 0; classIndex < size; classIndex++) {
                int redStar = population[i][classIndex];
                if (flagCopy[classIndex][redStar] != 0) {
                    costValue[i]++;
                    populationFlag[i][classIndex] = -1;
                }
                //nếu data đúng
                else {
                    populationFlag[i][classIndex] = 0;
                    flagCopy[classIndex][redStar] = 1;
                    //2 sao đỏ cùng lớp không chấm cùng 1 lớp
                    if(classIndex%2 == 0){
                        flagCopy[classIndex+1][redStar] = 1;
                        int classOfRedstar = indexClassOfRedStar[redStar];
                        for (int redStarOfClass : indexRedStarOfClass[classOfRedstar]) {
                            flagCopy[classIndex + 1][redStarOfClass] = 1;
                        }
                    }
                    int k = 0;
                    if (classIndex != 0) k = classIndex/2;
                    //sao đỏ của 2 lớp không chấm chéo nhau
                    for (int redStarOfClass : indexRedStarOfClass[k]){
                        flagCopy[indexClassOfRedStar[redStar]*2][redStarOfClass] = 1;
                        flagCopy[indexClassOfRedStar[redStar]*2+1][redStarOfClass] = 1;
                    }
                }
            }
        }
    }

    public void Print() {
        int [] temp = costValue.clone();
        Arrays.sort(temp);
        int best = temp[0];
        for (int i=0;i<n;i++){
            if (costValue[i]==best){
                if(best < costValueOutput){
                    outputData = population[i];
                    costValueOutput = best;
                }
                break;
            }
        }
    }

    public void chonloc() {
        int [] temp = costValue.clone();
        Arrays.sort(temp);
        int nguong = temp[n*50/100];
        for (int i=0;i<n;i++){
            if (costValue[i]>nguong){
                population[i]=population[ran.nextInt(n)].clone();
            }
        }
    }

    public void dotbien() {
        for (int k=0;k<n/2;k++){
            int i=ran.nextInt(n);
            for (int j=0;j<size;j++)
                if(populationFlag[i][j] == -1){
                    int value = ran.nextInt(size);
                    int temp=population[i][j];
                    population[i][j] = population[i][value];
                    population[i][value] = temp;
                }
        }
    }

}
