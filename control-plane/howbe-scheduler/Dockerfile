FROM python:3.12-slim

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir -r requirements.txt

#COPY wait-for-it.sh /app/wait-for-it.sh
#RUN sed -i 's/\r$//' /app/wait-for-it.sh
#RUN chmod +x /app/wait-for-it.sh

COPY . .

CMD ["python", "-u", "main.py"]
