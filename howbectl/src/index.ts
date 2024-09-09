#!/usr/bin/env node

import { PodStatusDTO, createPodStatusDTO } from './interfaces/pod_status.interface'
import { printPodStatus, isValidJSON } from './utils/print'
const { Command } = require("commander");
const inquirer = require('inquirer');
const pkg = require('../package.json');
const axios = require('axios');
const Table = require('cli-table3');
const fs = require('fs');
const yaml = require('js-yaml');


const masterNode = {
  ip : "localhost",
  port : "8081"
}

const program = new Command();

program
  .name('howbectl')
  .description('CLI tool to manage container')
  .version('1.0.0');


const getCommand =
  program
    .command('get')
    .description("Get object's information");

async function getPods() {
  try {
    const response = await axios.get(
      `http://${masterNode.ip}:${masterNode.port}/api/v1/pods`
    );
    const data : string[] = response.data;
    if(!isValidJSON(data[0])){
      console.log("No pods found");
      return;
    }
    const input_data : PodStatusDTO[] = data.map((pod : string) => JSON.parse(pod) as PodStatusDTO)
    printPodStatus(input_data)
  } catch (error : any) {
    console.error('Error:', error.message);
  }
}

async function getPodByName(name : string){
  try {
    const response = await axios.get(
      `http://${masterNode.ip}:${masterNode.port}/api/v1/pods/${name}`
    );
    if(response.data == ""){
      console.log("No pods found");
      return;
    }
    const data : PodStatusDTO = response.data;
    printPodStatus([data])
  } catch (error : any) {
    console.error('Error:', error.message);
  }
}

getCommand
  .argument('<resource>', 'Resource to get (e.g., pods)')
  .argument('[name]', 'Name of the specific resource to get (optional)')
  .action(async (resource: string, name?: string) => {
    if (resource === "pods") {
      if(name){
        await getPodByName(name);
      }else{
        await getPods();
      }
    } 
    else {
      console.log(`Resource ${resource} is not supported.`);
    }
  });

const deleteCommand =
  program
    .command('delete')
    .description('Delete object');

async function deletePod(name : string){
  try {
    const response = await axios.delete(
      `http://${masterNode.ip}:${masterNode.port}/api/v1/pods/${name}`
    );
    const data = response.data;
    console.log(data);
  } catch (error : any) {
    console.error('Error:', error.message);
  }
}

deleteCommand
  .argument('<resource>', 'Resource to delete (e.g., pods)')
  .argument('<name>', 'Name of the specific resource to delete')
  .action(async (resource: string, name: string) => {
    if (resource === "pods") {
      await deletePod(name);
    } else {
      console.log(`Resource ${resource} is not supported.`);
    }
  });

const createCommand =
  program
    .command('create')
    .description('Create a resource from a file')
    .option('-f, --filename <filePath>', 'Path to the YAML file')


async function createPod (data : any){
  const request : PodStatusDTO = createPodStatusDTO(data)
  try {
    const response = await axios.post(
      `http://${masterNode.ip}:${masterNode.port}/api/v1/pods`,
      request,
      {
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    console.log(response.data);
    } catch (error : any) {
      console.error('Error:', error.message);
      }
}
//create
createCommand
  .action(async (cmd : any) => {
    let filePath = cmd.filename;

    if (!filePath) {
      const answers = await inquirer.prompt([
        {
          type: 'input',
          name: 'filePath',
          message: 'Please provide the path to the YAML file:',
          validate: (input : any) => {
            if (fs.existsSync(input)) {
              return true;
            }
            return 'File does not exist. Please provide a valid file path.';
          }
        }
      ]);
      filePath = answers.filePath;
    }

    let data;
    try {
      const fileContents = fs.readFileSync(filePath, 'utf8');
      data = yaml.load(fileContents);
    } catch (error) {
      console.error(`There is no file in ${filePath}`);
      return;
    }

    const kind : string = data.kind;

    switch (kind) {
      case "Pod":
        createPod(data);
        break;
    }

  })

  

program.parse(process.argv);