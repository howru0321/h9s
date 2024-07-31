#!/usr/bin/env node

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


const getCommand = program.command('get').description("Get object's information");

const deleteCommand = program.command('delete').description('Delete object');

async function podCreate (data : any){
  const podName : string = data.metadata.name;
  const containers = data.spec.containers;

  try {
    const response = await axios.post(`http://${masterNode.ip}:${masterNode.port}/api/v1/pod`, {
      name: podName,
      containers: containers
    });
    console.log('Response:', response.data);
    } catch (error : any) {
      console.error('Error:', error.message);
      }
}
//create
program
  .command('create')
  .description('Create a resource from a file')
  .option('-f, --filename <filePath>', 'Path to the YAML file')
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
        podCreate(data);
        break;
    }

  })

  

program.parse(process.argv);